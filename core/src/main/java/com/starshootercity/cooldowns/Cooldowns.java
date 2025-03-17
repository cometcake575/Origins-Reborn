package com.starshootercity.cooldowns;

import com.starshootercity.events.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import com.starshootercity.events.PlayerSwapOriginEvent;
import com.starshootercity.util.ShortcutUtils;
import com.starshootercity.util.config.ConfigManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class Cooldowns implements Listener {
    private final Map<NamespacedKey, CooldownInfo> registeredCooldowns = new HashMap<>();
    private final NamespacedKey cooldownKey = new NamespacedKey(OriginsReborn.getInstance(), "cooldowns");
    private final NamespacedKey hasCooldownKey = new NamespacedKey(OriginsReborn.getInstance(), "has_cooldown");

    @EventHandler
    public void onPlayerSwapOrigin(PlayerSwapOriginEvent event) {
        event.getPlayer().getPersistentDataContainer().remove(cooldownKey);
    }

    private static @Nullable String getTime(int cooldownTime) {
        if (cooldownTime <= 0) return null;
        String time;
        if (Math.floorDiv(cooldownTime, 60) == 0) {
            time = "%ss".formatted(cooldownTime);
        } else {
            time = "%sm %ss".formatted(Math.floorDiv(cooldownTime, 60), cooldownTime % 60);
        }
        return time;
    }

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (ShortcutUtils.isBedrockPlayer(player.getUniqueId())) {
                StringBuilder s = new StringBuilder();
                for (NamespacedKey key : getCooldowns(player)) {
                    CooldownInfo info = registeredCooldowns.get(key);
                    if (info.getIcon() == null) continue;
                    int amount = (int) (getCooldown(player, key) / 50);
                    s.append(getTime(amount)).append(" ");
                }
                player.sendActionBar(Component.text(s.toString()));
                continue;
            }
            Component message = Component.empty();
            int num = 0;
            Entity vehicle = player.getVehicle();
            if (vehicle != null) {
                if (vehicle instanceof LivingEntity entity) {
                    AttributeInstance instance = entity.getAttribute(OriginsReborn.getNMSInvoker().getMaxHealthAttribute());
                    if (instance != null) {
                        num += (int) (Math.floor((instance.getValue() - 1) / 10) - 1);
                    }
                }
            }
            if (player.getRemainingAir() < player.getMaximumAir() || OriginsReborn.getNMSInvoker().isUnderWater(player)) num++;
            int i = 0;
            for (NamespacedKey key : getCooldowns(player)) {
                CooldownInfo info = registeredCooldowns.get(key);
                if (info.getIcon() == null) continue;
                i++;
                float amount = getCooldown(player, key) / (info.getCooldownTime() * 50f);
                if (!info.isReversed()) amount = 1 - amount;
                message = message.append(Component.text("\uF004")).append(formCooldownBar(amount, info, num));
                num++;
            }
            if (i == 0) {
                if (!player.getPersistentDataContainer().has(hasCooldownKey, OriginSwapper.BooleanPDT.BOOLEAN)) continue;
                player.getPersistentDataContainer().remove(hasCooldownKey);
            }
            player.getPersistentDataContainer().set(hasCooldownKey, OriginSwapper.BooleanPDT.BOOLEAN, true);
            player.sendActionBar(OriginsReborn.getNMSInvoker().applyFont(Component.text("\uF003"), Key.key("minecraft:cooldown_bar/height_0")).append(message));
        }
    }

    public Component formCooldownBar(float percentage, CooldownInfo info, int height) {
        return iconDataMap.get(info.getIcon()).assemble(percentage, height);
    }

    public long getCooldown(Player player, NamespacedKey key) {
        PersistentDataContainer pdc = player.getPersistentDataContainer().getOrDefault(cooldownKey, PersistentDataType.TAG_CONTAINER, player.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
        CooldownInfo info = registeredCooldowns.get(key);
        if (info == null) return 0;
        return Math.max(0, pdc.getOrDefault(key, PersistentDataType.LONG, 0L) - (info.isStatic() ? 0 : Instant.now().toEpochMilli()));
    }

    public List<NamespacedKey> getCooldowns(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer().getOrDefault(cooldownKey, PersistentDataType.TAG_CONTAINER, player.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
        List<NamespacedKey> keys = new ArrayList<>(pdc.getKeys());
        keys.removeIf(key -> !registeredCooldowns.containsKey(key) || (!hasCooldown(player, key) && !registeredCooldowns.get(key).isStatic()));
        return keys;
    }

    public void resetCooldowns(Player player) {
        player.getPersistentDataContainer().remove(cooldownKey);
    }

    public boolean hasCooldown(Player player, NamespacedKey key) {
        return getCooldown(player, key) > 0;
    }

    public void setCooldown(Player player, NamespacedKey key, int cooldown) {
        if (cooldown == -1) return;
        CooldownInfo info = registeredCooldowns.get(key);
        PersistentDataContainer pdc = player.getPersistentDataContainer().getOrDefault(cooldownKey, PersistentDataType.TAG_CONTAINER, player.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
        pdc.set(key, PersistentDataType.LONG, info.isStatic ? cooldown * 50L : Instant.now().toEpochMilli() + (cooldown * 50L));
        player.getPersistentDataContainer().set(cooldownKey, PersistentDataType.TAG_CONTAINER, pdc);
    }

    public void setCooldown(Player player, NamespacedKey key) {
        CooldownInfo info = registeredCooldowns.get(key);
        setCooldown(player, key, info.getCooldownTime());
    }

    public record CooldownIconData(List<Component> barPieces, Component icon) {
        public Component assemble(float completion, int height) {
            double num = Math.floor(barPieces.size() * completion);
            Component result = icon.append(Component.text("\uF002"));
            for (int i = 0; i < barPieces.size(); i++) {
                result = result.append((i <= num ? barPieces : emptyBarPieces).get(i));
                result = result.append(Component.text("\uF001"));
            }
            @Subst("minecraft:cooldown_bar/height_0") String formatted = "minecraft:cooldown_bar/height_%s".formatted(height);
            return OriginsReborn.getNMSInvoker().applyFont(result, Key.key(formatted));
        }
    }

    public static List<Component> emptyBarPieces;

    public CooldownIconData makeCID(File file) {
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new CooldownIconData(makeBarPieces(image), makeIcon(image));
    }

    public Component makeIcon(BufferedImage image) {
        BufferedImage iconImage = image.getSubimage(73, 0, 8, 8);
        Component icon = Component.empty();
        String pixels = "\uE000\uE001\uE002\uE003\uE004\uE005\uE006\uE007";
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                int col = iconImage.getRGB(x, y);
                if (col == 0) {
                    icon = icon.append(Component.text("\uF002"));
                } else icon = icon.append(Component.text(pixels.charAt(y)).color(TextColor.color(col)));
                icon = icon.append(Component.text(y == 7 ? "\uF001" : "\uF000"));
            }
        }
        return icon;
    }

    public List<Component> makeBarPieces(BufferedImage image) {
        BufferedImage barImage = image.getSubimage(0, 2, 71, 5);
        String pixels = "\uE002\uE003\uE004\uE005\uE006";
        List<Component> result = new ArrayList<>();
        for (int x = 0; x < 71; x++) {
            Component c = Component.empty();
            for (int y = 0; y < 5; y++) {
                int col = barImage.getRGB(x, y);
                if (col == 0) {
                    c = c.append(Component.text("\uF002"));
                } else c = c.append(Component.text(pixels.charAt(y)).color(TextColor.color(col)));
                if (y != 4) c = c.append(Component.text("\uF000"));
            }
            result.add(c);
        }
        return result;
    }

    public final Map<String, CooldownIconData> iconDataMap = new HashMap<>();

    public NamespacedKey registerCooldown(JavaPlugin instance, NamespacedKey key, CooldownInfo info) {
        if (ConfigManager.getConfigValue(ConfigManager.Option.DISABLE_ALL_COOLDOWNS)) return key;

        if (info.getIcon() != null && ConfigManager.getConfigValue(ConfigManager.Option.SHOW_COOLDOWN_ICONS)) {
            File icon = new File(instance.getDataFolder(), "icons/%s.png".formatted(info.getIcon()));
            if (!icon.exists()) {
                boolean ignored = icon.getParentFile().mkdirs();
                instance.saveResource("icons/%s.png".formatted(info.getIcon()), false);
            }
            CooldownIconData iconData = makeCID(icon);
            iconDataMap.put(info.getIcon(), iconData);
        }
        registeredCooldowns.put(key, info);
        return key;
    }

    public Cooldowns() {
        File icon = new File(OriginsReborn.getInstance().getDataFolder(), "icons/empty_bar.png");

        if (!icon.exists()) {
            boolean ignored = icon.getParentFile().mkdirs();
            OriginsReborn.getInstance().saveResource("icons/empty_bar.png", false);
        }

        BufferedImage image;
        try {
            image = ImageIO.read(icon);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        emptyBarPieces = makeBarPieces(image);
    }

    @SuppressWarnings("unused")
    public static class CooldownInfo {
        private int cooldownTime;
        private final boolean reversed;
        private final @Nullable String icon;
        private final boolean isStatic;

        public CooldownInfo(int cooldownTime, @Nullable String icon, boolean reversed, boolean isStatic) {
            this.cooldownTime = cooldownTime;
            this.icon = icon;
            this.reversed = reversed;
            this.isStatic = isStatic;
        }

        public boolean isStatic() {
            return isStatic;
        }

        public boolean isReversed() {
            return reversed;
        }

        public CooldownInfo(int cooldownTime, @Nullable String icon, boolean reversed) {
            this(cooldownTime, icon, reversed, false);
        }

        public CooldownInfo(int cooldownTime, @Nullable String icon) {
            this(cooldownTime, icon, false, false);
        }

        public CooldownInfo(int cooldownTime, boolean reversed) {
            this(cooldownTime, null, reversed, false);
        }

        public CooldownInfo(int cooldownTime) {
            this(cooldownTime, null, false, false);
        }

        public @Nullable String getIcon() {
            return icon;
        }

        public void setCooldownTime(int cooldownTime) {
            this.cooldownTime = cooldownTime;
        }

        public int getCooldownTime() {
            return cooldownTime;
        }
    }
}

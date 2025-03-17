package com.starshootercity.abilities;

import com.starshootercity.OriginsAddon;
import com.starshootercity.OriginsReborn;
import com.starshootercity.commands.FlightToggleCommand;
import com.starshootercity.cooldowns.CooldownAbility;
import com.starshootercity.util.config.ConfigManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityRegister {
    public static Map<Key, Ability> abilityMap = new HashMap<>();
    public static Map<Key, OriginsAddon> pluginMap = new HashMap<>();
    public static Map<Key, DependencyAbility> dependencyAbilityMap = new HashMap<>();
    public static Map<Key, List<MultiAbility>> multiAbilityMap = new HashMap<>();
    public static List<BreakSpeedModifierAbility> breakSpeedModifierAbilities = new ArrayList<>();
    public static List<AttributeModifierAbility> attributeModifierAbilities = new ArrayList<>();
    public static List<SkinChangingAbility> skinChangingAbilities = new ArrayList<>();

    public static Map<Key, List<AbilityRunnable>> runOnRegisters = new HashMap<>();

    private static File abilityFile;
    private static FileConfiguration abilityFileConfig;

    public static <T> void registerConfigOption(OriginsAddon addon, Ability ability, ConfigManager.SettingType<T> settingType, String path, List<String> comments, T defaultValue) {
        String pathToUse = addon.getNamespace() + "." + ability.getKey().value() + "." + path;
        if (abilityFileConfig.contains(pathToUse)) return;

        settingType.set(abilityFileConfig, pathToUse, defaultValue);

        OriginsReborn.getNMSInvoker().setComments(abilityFileConfig, pathToUse, comments);

        try {
            abilityFileConfig.save(abilityFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<String, Object> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T getConfigOption(OriginsAddon addon, Ability ability, ConfigManager.SettingType<T> settingType, String path) {
        return (T) cache.computeIfAbsent(
                addon.getNamespace() + "." + ability.getKey().value() + "." + path,
                s -> settingType.get(abilityFileConfig, s));
    }

    public static void reloadAbilityConfig() {
        cache.clear();
        try {
            abilityFileConfig.load(abilityFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setupAbilityConfig() {
        abilityFile = new File(OriginsReborn.getInstance().getDataFolder(), "ability-config.yml");

        if (!abilityFile.exists()) {
            boolean ignored = abilityFile.getParentFile().mkdirs();
            OriginsReborn.getInstance().saveResource("ability-config.yml", false);
        }

        abilityFileConfig = new YamlConfiguration();

        reloadAbilityConfig();
    }

    public static void registerAbility(Ability ability, JavaPlugin instance) {

        if (ability instanceof SkinChangingAbility skinChangingAbility) {
            if (!OriginsReborn.isSkinManagerEnabled() && !skinChangingAbility.forceEnabled()) return;
            skinChangingAbilities.add(skinChangingAbility);
        }

        if (instance instanceof OriginsAddon addon) {
            pluginMap.put(ability.getKey(), addon);
        }

        if (runOnRegisters.containsKey(ability.getKey())) {
            for (AbilityRunnable r : runOnRegisters.get(ability.getKey())) r.run(ability);
        }

        ability.initialize();

        if (ability instanceof DependencyAbility dependencyAbility) {
            dependencyAbilityMap.put(ability.getKey(), dependencyAbility);
        }
        if (ability instanceof MultiAbility multiAbility) {
            for (Ability a : multiAbility.getAbilities()) {
                multiAbilityMap.computeIfAbsent(a.getKey(), key -> new ArrayList<>()).add(multiAbility);
            }
        }
        if (ability instanceof CooldownAbility cooldownAbility) {
            cooldownAbility.setupCooldownConfig(instance);
        }
        if (ability instanceof Listener listener) {
            Bukkit.getPluginManager().registerEvents(listener, instance);
        }
        if (ability instanceof VisibleAbility visibleAbility) {
            visibleAbility.setupTranslatedText();
        }
        if (ability instanceof AttributeModifierAbility ama) {
            ama.setupAttributeConfig();
            attributeModifierAbilities.add(ama);
        }
        if (ability instanceof BreakSpeedModifierAbility breakSpeedModifierAbility) {
            breakSpeedModifierAbilities.add(breakSpeedModifierAbility);
        }
        abilityMap.put(ability.getKey(), ability);
    }


    /**
     * @deprecated Testing abilities is now contained in the Ability interface
     */
    @Deprecated(forRemoval = true)
    public static void runForAbility(Entity entity, Key key, Runnable runnable) {
        runForAbility(entity, key, runnable, () -> {});
    }

    /**
     * @deprecated Testing abilities is now contained in the Ability interface
     */
    @Deprecated(forRemoval = true)
    public static boolean hasAbility(Player player, Key key) {
        return hasAbility(player, key, false);
    }

    /**
     * @deprecated Testing abilities is now contained in the Ability interface
     */
    @Deprecated(forRemoval = true)
    public static boolean hasAbility(Player player, Key key, boolean ignoreOverrides) {
        if (!abilityMap.containsKey(key)) return false;
        return abilityMap.get(key).hasAbility(player);
    }

    /**
     * @deprecated Testing abilities is now contained in the Ability interface
     */
    @Deprecated(forRemoval = true)
    public static void runForAbility(Entity entity, Key key, Runnable runnable, Runnable other) {
        if (entity == null) return;
        String worldId = entity.getWorld().getName();
        if (OriginsReborn.getInstance().getConfig().getStringList("worlds.disabled-worlds").contains(worldId)) return;
        if (entity instanceof Player player) {
            if (hasAbility(player, key)) {
                runnable.run();
                return;
            }
        }
        other.run();
    }

    /**
     * @deprecated Testing abilities is now contained in the Ability interface
     */
    @Deprecated(forRemoval = true)
    public static void runWithoutAbility(Entity entity, Key key, Runnable runnable) {
        runForAbility(entity, key, () -> {}, runnable);
    }


    public static boolean canFly(Player player, boolean disabledWorld) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || FlightToggleCommand.canFly(player)) return true;
        if (disabledWorld) return false;
        for (Ability ability : AbilityRegister.abilityMap.values()) {
            if (ability instanceof FlightAllowingAbility flightAllowingAbility) {
                if (ability.hasAbility(player) && flightAllowingAbility.canFly(player)) return true;
            }
        }
        return false;
    }


    public static boolean isInvisible(Player player) {
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return true;
        for (Ability ability : abilityMap.values()) {
            if (ability instanceof VisibilityChangingAbility visibilityChangingAbility) {
                if (ability.hasAbility(player) && visibilityChangingAbility.isInvisible(player)) return true;
            }
        }
        return false;
    }

    public static void updateFlight(Player player, boolean inDisabledWorld) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || FlightToggleCommand.canFly(player)) {
            player.setFlySpeed(0.1f);
            return;
        }
        if (inDisabledWorld) return;
        TriState flyingFallDamage = TriState.FALSE;
        float speed = -1f;
        for (Ability ability : abilityMap.values()) {
            if (ability instanceof FlightAllowingAbility flightAllowingAbility) {
                if (ability.hasAbility(player) && flightAllowingAbility.canFly(player)) {
                    float abilitySpeed = flightAllowingAbility.getFlightSpeed(player);
                    speed = speed == -1 ? abilitySpeed : Math.min(speed, abilitySpeed);
                    if (flightAllowingAbility.getFlyingFallDamage(player) == TriState.TRUE) {
                        flyingFallDamage = TriState.TRUE;
                    }
                }
            }
        }
        OriginsReborn.getNMSInvoker().setFlyingFallDamage(player, flyingFallDamage);
        player.setFlySpeed(speed == -1 ? 0 : speed);
    }

    public static void updateEntity(Player player, Entity target) {
        byte data = 0;
        if (target.getFireTicks() > 0) {
            data += 0x01;
        }
        if (target.isGlowing()) {
            data += 0x40;
        }
        if (target instanceof LivingEntity entity) {
            if (entity.isInvisible()) data += 0x20;
        }
        if (target instanceof Player targetPlayer) {
            if (targetPlayer.isSneaking()) {
                data += 0x02;
            }
            if (targetPlayer.isSprinting()) {
                data += 0x08;
            }
            if (targetPlayer.isSwimming()) {
                data += 0x10;
            }
            if (targetPlayer.isGliding()) {
                data += (byte) 0x80;
            }
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                try {
                    ItemStack item = targetPlayer.getInventory().getItem(equipmentSlot);
                    if (item != null) {
                        player.sendEquipmentChange(targetPlayer, equipmentSlot, item);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }

        OriginsReborn.getNMSInvoker().sendEntityData(player, target, data);
    }

    public interface AbilityRunnable {
        void run(Ability ability);
    }
}

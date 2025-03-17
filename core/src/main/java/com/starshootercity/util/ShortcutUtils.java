package com.starshootercity.util;

import com.starshootercity.OriginsReborn;
import com.starshootercity.events.ServerTickEndEvent;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.event.block.Action;
import org.geysermc.api.Geyser;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("unused")
public class ShortcutUtils {

    private static BukkitAudiences adventure;

    public static BukkitAudiences adventure() {
        return adventure;
    }

    public static void initialize(JavaPlugin plugin) {
        adventure = BukkitAudiences.create(plugin);
    }

    public static void shutdown(JavaPlugin plugin) {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    public static Set<Action> LEFT_CLICK = Set.of(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK);
    public static Set<Action> RIGHT_CLICK = Set.of(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    public static void giveItemWithDrops(Player player, ItemStack... itemStacks) {
        for (ItemStack i : player.getInventory().addItem(itemStacks).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), i);
        }
    }

    public static @Nullable LivingEntity getLivingDamageSource(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity entity) return entity;
        else if (event.getDamager() instanceof LivingEntity entity) return entity;
        return null;
    }

    public static JSONObject openJSONFile(File file) {
        try (Scanner scanner = new Scanner(file)) {
            StringBuilder data = new StringBuilder();
            while (scanner.hasNextLine()) {
                data.append(scanner.nextLine());
            }
            try {
                return new JSONObject(data.toString());
            } catch (JSONException e) {
                return new JSONObject();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isBedrockPlayer(UUID uuid) {
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
        } catch (NoClassDefFoundError e) {
            try {
                return Geyser.api().isBedrockPlayer(uuid);
            } catch (NoClassDefFoundError ex) {
                return false;
            }
        }
    }

    public static Component getColored(String f) {
        Component component = Component.empty();
        Iterator<String> iterator = substringsBetween(f, "<", ">").iterator();
        for (String s : f.split("<#\\w{6}>")) {
            if (s.isEmpty()) continue;
            component = component.append(iterator.hasNext() ? Component.text(s).color(TextColor.fromHexString(iterator.next())) : Component.text(s));
        }
        return component;
    }

    public static List<String> substringsBetween(String s, String start, String end) {
        int starti = s.indexOf(start);
        if (starti == -1) return List.of();
        String startPart = s.substring(starti);
        int endi = startPart.indexOf(end);
        if (endi == -1) return List.of();
        List<String> data = new ArrayList<>();
        data.add(startPart.substring(0, endi));
        data.addAll(substringsBetween(startPart.substring(endi), start, end));
        return data;
    }

    public static boolean isInfinite(PotionEffect effect) {
        if (OriginsReborn.getNMSInvoker().supportsInfiniteDuration()) {
            return (effect.getDuration() == -1);
        } else return (effect.getDuration() >= 20000);
    }

    public static int infiniteDuration() {
        if (OriginsReborn.getNMSInvoker().supportsInfiniteDuration()) {
            return -1;
        } else return 50000;
    }

    public static int getCurrentTick() {
        return ServerTickEndEvent.getTickCount();
    }
}

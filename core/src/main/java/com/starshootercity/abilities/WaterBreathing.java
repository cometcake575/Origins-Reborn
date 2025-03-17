package com.starshootercity.abilities;

import com.starshootercity.events.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import com.starshootercity.util.ShortcutUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WaterBreathing implements Listener, VisibleAbility {
    @EventHandler
    public void onEntityAirChange(EntityAirChangeEvent event) {
        runForAbility(event.getEntity(), player -> {
            if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(airKey, OriginSwapper.BooleanPDT.BOOLEAN)))
                return;
            if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(dehydrationKey, OriginSwapper.BooleanPDT.BOOLEAN)))
                return;
            if (player.getRemainingAir() - event.getAmount() > 0) {
                if (!OriginsReborn.getNMSInvoker().isUnderWater(player) && !hasWaterBreathing(player)) return;
            } else if (OriginsReborn.getNMSInvoker().isUnderWater(player) || hasWaterBreathing(player)) return;
            event.setCancelled(true);
        });
    }

    @EventHandler
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (event.getCause() != EntityPotionEffectEvent.Cause.TURTLE_HELMET) return;
        runForAbility(event.getEntity(), player -> event.setCancelled(true));
    }

    public boolean hasWaterBreathing(Player player) {
        return player.hasPotionEffect(PotionEffectType.CONDUIT_POWER) || player.hasPotionEffect(PotionEffectType.WATER_BREATHING);
    }

    NamespacedKey airKey = new NamespacedKey(OriginsReborn.getInstance(), "fullair");
    NamespacedKey dehydrationKey = new NamespacedKey(OriginsReborn.getInstance(), "dehydrating");
    NamespacedKey damageKey = new NamespacedKey(OriginsReborn.getInstance(), "ignore-item-damage");

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            runForAbility(p, player -> {
                if (OriginsReborn.getNMSInvoker().isUnderWater(player) || hasWaterBreathing(player) || player.isInRain()) {
                    ItemStack helmet = player.getInventory().getHelmet();
                    if (helmet != null && OriginsReborn.getNMSInvoker().isUnderWater(player)) {
                        if (helmet.getType() == Material.TURTLE_HELMET) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 200, 0, false, false, true));
                        }
                    }
                    if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(airKey, OriginSwapper.BooleanPDT.BOOLEAN))) {
                        player.setRemainingAir(-50);
                        return;
                    }
                    player.setRemainingAir(Math.min(Math.max(player.getRemainingAir() + 4, 4), player.getMaximumAir()));
                    if (player.getRemainingAir() == player.getMaximumAir()) {
                        player.setRemainingAir(-50);
                        player.getPersistentDataContainer().set(airKey, OriginSwapper.BooleanPDT.BOOLEAN, true);
                    }
                } else {
                    if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(airKey, OriginSwapper.BooleanPDT.BOOLEAN))) {
                        player.setRemainingAir(player.getMaximumAir());
                        player.getPersistentDataContainer().set(airKey, OriginSwapper.BooleanPDT.BOOLEAN, false);
                    }
                    decreaseAir(player);
                    if (player.getRemainingAir() < -25) {
                        player.getPersistentDataContainer().set(dehydrationKey, OriginSwapper.BooleanPDT.BOOLEAN, true);
                        player.setRemainingAir(-5);
                        player.getPersistentDataContainer().set(dehydrationKey, OriginSwapper.BooleanPDT.BOOLEAN, false);
                        player.getPersistentDataContainer().set(damageKey, PersistentDataType.INTEGER, ShortcutUtils.getCurrentTick());
                        OriginsReborn.getNMSInvoker().dealDrowningDamage(player, 2);
                    }
                }
            }, player -> {
                if (player.getPersistentDataContainer().has(airKey, OriginSwapper.BooleanPDT.BOOLEAN)) {
                    player.setRemainingAir(player.getMaximumAir());
                    player.getPersistentDataContainer().remove(airKey);
                }
            });
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getPlayer().getPersistentDataContainer().getOrDefault(damageKey, PersistentDataType.INTEGER, 0) >= ShortcutUtils.getCurrentTick()) {
            event.deathMessage(event.getPlayer().displayName().append(Component.text(" didn't manage to keep wet")));
        }
    }

    private final Random random = new Random();

    public void decreaseAir(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && helmet.getItemMeta() != null) {
            int respirationLevel = helmet.getItemMeta().getEnchantLevel(OriginsReborn.getNMSInvoker().getRespirationEnchantment());
            if (respirationLevel > 0) {
                if (random.nextInt(respirationLevel + 1) > 0) return;
            }
        }
        player.setRemainingAir(player.getRemainingAir() - 1);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().getPersistentDataContainer().set(damageKey, PersistentDataType.INTEGER, -1);
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:water_breathing");
    }

    @Override
    public String description() {
        return "You can breathe underwater, but not on land.";
    }

    @Override
    public String title() {
        return "Gills";
    }
}

package com.starshootercity.abilities;

import com.starshootercity.events.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import com.starshootercity.SavedPotionEffect;
import com.starshootercity.util.ShortcutUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class WaterVision implements Listener, VisibleAbility {
    Map<Player, SavedPotionEffect> storedEffects = new HashMap<>();

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            runForAbility(p, player -> {
                if (OriginsReborn.getNMSInvoker().isUnderWater(player)) {
                    PotionEffect effect = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
                    boolean ambient = false;
                    boolean showParticles = false;
                    if (effect != null) {
                        ambient = effect.isAmbient();
                        showParticles = effect.hasParticles();
                        if (!ShortcutUtils.isInfinite(effect)) {
                            storedEffects.put(player, new SavedPotionEffect(effect, ShortcutUtils.getCurrentTick()));
                            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                        }
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, ShortcutUtils.infiniteDuration(), -1, ambient, showParticles));
                } else {
                    if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                        PotionEffect effect = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
                        if (effect != null) {
                            if (ShortcutUtils.isInfinite(effect)) player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                        }
                    }
                    if (storedEffects.containsKey(player)) {
                        SavedPotionEffect effect = storedEffects.get(player);
                        storedEffects.remove(player);
                        PotionEffect potionEffect = effect.effect();
                        int time = potionEffect.getDuration() - (ShortcutUtils.getCurrentTick() - effect.currentTime());
                        if (time > 0) {
                            player.addPotionEffect(new PotionEffect(
                                    potionEffect.getType(),
                                    time,
                                    potionEffect.getAmplifier(),
                                    potionEffect.isAmbient(),
                                    potionEffect.hasParticles()
                            ));
                        }
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            storedEffects.remove(event.getPlayer());
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:water_vision");
    }

    @Override
    public String description() {
        return "Your vision underwater is perfect.";
    }

    @Override
    public String title() {
        return "Wet Eyes";
    }
}

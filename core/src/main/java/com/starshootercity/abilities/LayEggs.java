package com.starshootercity.abilities;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LayEggs implements Listener, VisibleAbility {
    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        if (event.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                runForAbility(p, player -> {
                    if (player.isSleeping()) {
                        player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.EGG));
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, SoundCategory.PLAYERS, 1, 1);
                    }
                });
            }
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:lay_eggs");
    }

    @Override
    public String description() {
        return "Whenever you wake up in the morning, you will lay an egg.";
    }

    @Override
    public String title() {
        return "Oviparous";
    }
}

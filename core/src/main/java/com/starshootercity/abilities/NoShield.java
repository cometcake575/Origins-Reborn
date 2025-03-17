package com.starshootercity.abilities;

import com.starshootercity.events.ServerTickEndEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class NoShield implements Listener, VisibleAbility {
    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            runForAbility(p, player -> player.setCooldown(Material.SHIELD, 1000));
        }
    }
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:no_shield");
    }

    @Override
    public String description() {
        return "The way your hands are formed provide no way of holding a shield upright.";
    }

    @Override
    public String title() {
        return "Unwieldy";
    }
}

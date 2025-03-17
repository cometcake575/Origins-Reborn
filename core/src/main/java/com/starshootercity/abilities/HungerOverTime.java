package com.starshootercity.abilities;

import com.starshootercity.events.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import com.starshootercity.util.config.ConfigManager;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class HungerOverTime implements DependantAbility, Listener, VisibleAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:hunger_over_time");
    }

    @Override
    public String description() {
        return "Being phantomized causes you to become hungry.";
    }

    @Override
    public String title() {
        return "Fast Metabolism";
    }

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        if (event.getTickNumber() % 20 != 0) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            runForAbility(p, player -> player.setExhaustion(player.getExhaustion() + getConfigOption(OriginsReborn.getInstance(), metabolismIncrease, ConfigManager.SettingType.FLOAT)));
        }
    }

    @Override
    public @NotNull Key getDependencyKey() {
        return Key.key("origins:phantomize");
    }

    private final String metabolismIncrease = "metabolism_increase";

    @Override
    public void initialize() {
        registerConfigOption(OriginsReborn.getInstance(), metabolismIncrease, Collections.singletonList("The amount to add to the player's exhaustion each second"), ConfigManager.SettingType.FLOAT, 0.812f);
    }
}

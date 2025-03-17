package com.starshootercity.abilities;

import com.starshootercity.events.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import com.starshootercity.util.config.ConfigManager;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class SprintJump implements Listener, VisibleAbility {
    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            runForAbility(p,
                    player -> {
                        if (player.isSprinting()) {
                            player.addPotionEffect(new PotionEffect(OriginsReborn.getNMSInvoker().getJumpBoostEffect(), 5, getConfigOption(OriginsReborn.getInstance(), jumpStrength, ConfigManager.SettingType.INTEGER), false, false));
                        }
                    });
        }
    }
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:sprint_jump");
    }

    @Override
    public String description() {
        return "You are able to jump higher by jumping while sprinting.";
    }

    @Override
    public String title() {
        return "Strong Ankles";
    }

    private final String jumpStrength = "jump_strength";

    @Override
    public void initialize() {
        registerConfigOption(OriginsReborn.getInstance(), jumpStrength, Collections.singletonList("Strength of the Jump Boost effect to give"), ConfigManager.SettingType.INTEGER, 1);
    }
}

package com.starshootercity.abilities;

import com.starshootercity.events.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Claustrophobia implements Listener, VisibleAbility {
    private final Map<Player, Integer> stacks = new HashMap<>();

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            runForAbility(p, player -> {
                if (player.getLocation().getBlock().getRelative(BlockFace.UP, 2).isSolid()) {
                    stacks.put(player, Math.min(stacks.getOrDefault(player, -200) + 1, 3600));
                } else stacks.put(player, Math.max(stacks.getOrDefault(player, -200) - 1, -200));
                int time = stacks.getOrDefault(player, -200);
                if (time > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, time, 0, true, true, true));
                    player.addPotionEffect(new PotionEffect(OriginsReborn.getNMSInvoker().getSlownessEffect(), time, 0, true, true, true));
                }
            });
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            stacks.put(event.getPlayer(), Math.min(stacks.getOrDefault(event.getPlayer(), -200), 0));
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:claustrophobia");
    }

    @Override
    public String description() {
        return "Being somewhere with a low ceiling for too long will weaken you and make you slower.";
    }

    @Override
    public String title() {
        return "Claustrophobia";
    }
}

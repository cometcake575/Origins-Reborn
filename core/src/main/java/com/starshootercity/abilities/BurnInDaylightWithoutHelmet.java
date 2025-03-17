package com.starshootercity.abilities;

import com.destroystokyo.paper.MaterialTags;
import com.starshootercity.events.ServerTickEndEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BurnInDaylightWithoutHelmet implements Listener, VisibleAbility {
    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            runForAbility(p,
                    player -> {
                        Block block = player.getWorld().getHighestBlockAt(player.getLocation());
                        while ((MaterialTags.GLASS.isTagged(block) || (MaterialTags.GLASS_PANES.isTagged(block)) && block.getY() >= player.getLocation().getY())) {
                            block = block.getRelative(BlockFace.DOWN);
                        }
                        boolean height = block.getY() < player.getLocation().getY();
                        boolean isInOverworld = player.getWorld().getEnvironment().equals(World.Environment.NORMAL);
                        boolean day = player.getWorld().isDayTime();
                        ItemStack helm = player.getInventory().getHelmet();
                        if (helm != null) {
                            if (!helm.getType().isAir()) return;
                        }
                        if (height && isInOverworld && day && !player.isInWaterOrRainOrBubbleColumn()) {
                            player.setFireTicks(Math.max(player.getFireTicks(), 60));
                        }
                    });
        }
    }

    @Override
    public String description() {
        return "You burn in daylight, unless you wear a helmet.";
    }

    @Override
    public String title() {
        return "Photoallergic";
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:burn_in_day_without_helmet");
    }
}

package com.starshootercity.abilities;

import com.destroystokyo.paper.MaterialTags;
import com.starshootercity.events.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import com.starshootercity.util.config.ConfigManager;
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

import java.util.List;

public class BurnInDaylight implements DependantAbility, Listener, VisibleAbility {
    @Override
    public DependencyType getDependencyType() {
        return DependencyType.INVERSE;
    }

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        if (event.getTickNumber() % 20 != 0) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            runForAbility(p,
                    player -> {
                        Block block = player.getWorld().getHighestBlockAt(player.getLocation());
                        while ((MaterialTags.GLASS.isTagged(block) || (MaterialTags.GLASS_PANES.isTagged(block)) && block.getY() >= player.getLocation().getY())) {
                            block = block.getRelative(BlockFace.DOWN);
                        }
                        boolean height = block.getY() < player.getLocation().getY();
                        boolean isInOverworld = player.getWorld().getEnvironment() == World.Environment.NORMAL;
                        boolean day = player.getWorld().isDayTime();

                        if (!getConfigOption(OriginsReborn.getInstance(), burnWithHelmet, ConfigManager.SettingType.BOOLEAN)) {
                            ItemStack helm = player.getInventory().getHelmet();
                            if (helm != null) {
                                if (!helm.getType().isAir()) return;
                            }
                        }

                        if (height && isInOverworld && day && !player.isInWaterOrRainOrBubbleColumn()) {
                            player.setFireTicks(Math.max(player.getFireTicks(), 60));
                        }
                    });
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:burn_in_daylight");
    }

    @Override
    public String description() {
        return "You begin to burn in daylight if you are not invisible.";
    }

    @Override
    public String title() {
        return "Photoallergic";
    }

    @Override
    public @NotNull Key getDependencyKey() {
        return Key.key("origins:phantomize");
    }

    private final String burnWithHelmet = "burn_with_helmet";

    @Override
    public void initialize() {
        registerConfigOption(OriginsReborn.getInstance(), burnWithHelmet, List.of("Whether the player should burn even when wearing a helmet"), ConfigManager.SettingType.BOOLEAN, true);
    }
}

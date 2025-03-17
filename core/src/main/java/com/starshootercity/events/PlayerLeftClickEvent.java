package com.starshootercity.events;

import com.starshootercity.OriginsReborn;
import com.starshootercity.util.ShortcutUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class PlayerLeftClickEvent extends PlayerEvent {
    private final PlayerInteractEvent playerInteractEvent;
    public PlayerLeftClickEvent(PlayerInteractEvent event) {
        super(event.getPlayer());
        playerInteractEvent = event;
    }

    public @Nullable Location getInteractionPoint() {
        return playerInteractEvent.getInteractionPoint();
    }

    public boolean hasBlock() {
        return playerInteractEvent.hasBlock();
    }

    public boolean hasItem() {
        return playerInteractEvent.hasItem();
    }

    public @Nullable ItemStack getItem() {
        return playerInteractEvent.getItem();
    }

    public @NotNull Material getMaterial() {
        return playerInteractEvent.getMaterial();
    }

    public @Nullable Block getClickedBlock() {
        return playerInteractEvent.getClickedBlock();
    }

    public @Nullable BlockFace getBlockFace() {
        return playerInteractEvent.getBlockFace();
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public static class PlayerLeftClickEventListener implements Listener {
        Map<Player, Integer> lastInteractionTickMap = new HashMap<>();

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (!event.getAction().isLeftClick()) {
                return;
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> {
                if (lastInteractionTickMap.getOrDefault(event.getPlayer(), -1) >= ShortcutUtils.getCurrentTick()) return;
                lastInteractionTickMap.put(event.getPlayer(), ShortcutUtils.getCurrentTick());
                new PlayerLeftClickEvent(event).callEvent();
            });
        }

        @EventHandler
        public void onPlayerDropItem(PlayerDropItemEvent event) {
            lastInteractionTickMap.put(event.getPlayer(), ShortcutUtils.getCurrentTick()+1);
        }

        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            lastInteractionTickMap.put(event.getPlayer(), ShortcutUtils.getCurrentTick()+1);
        }
    }
}

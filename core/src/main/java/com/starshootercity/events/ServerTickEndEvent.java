package com.starshootercity.events;

import com.starshootercity.OriginsReborn;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class ServerTickEndEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static int getTickCount() {
        return tickCount;
    }

    private static int tickCount = 0;
    private final int tickNumber;

    @ApiStatus.Internal
    public ServerTickEndEvent() {
        tickCount++;
        this.tickNumber = tickCount;
    }

    /**
     * @return What tick this was since start (first tick = 1)
     */
    public int getTickNumber() {
        return this.tickNumber;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public static void initialize() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getPluginManager().callEvent(new ServerTickEndEvent());
            }
        }.runTaskTimer(OriginsReborn.getInstance(), 0, 1);
    }
}

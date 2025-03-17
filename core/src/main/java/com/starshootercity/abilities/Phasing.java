package com.starshootercity.abilities;

import com.starshootercity.events.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import com.starshootercity.util.config.ConfigManager;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Phasing implements DependantAbility, FlightAllowingAbility, BreakSpeedModifierAbility, Listener, VisibleAbility {

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:phasing");
    }

    @Override
    public @NotNull Key getDependencyKey() {
        return Key.key("origins:phantomize");
    }

    @Override
    public String description() {
        return "While phantomized, you can walk through solid material, except Obsidian.";
    }

    @Override
    public String title() {
        return "Phasing";
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onServerTick(ServerTickEndEvent event) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            runForAbility(p, player -> {
                boolean isInBlock = isInBlock(player);
                setPhasing(player, (player.isOnGround() && player.isSneaking() && !impassable.contains(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType())) || (isInBlock));
                OriginsReborn.getNMSInvoker().setNoPhysics(player, player.getGameMode() == GameMode.SPECTATOR || isPhasing.getOrDefault(player, false));
                if (isPhasing.getOrDefault(player, false)) {
                    player.setFallDistance(0);
                    if (player.getAllowFlight()) player.setFlying(true);
                }
            }, player -> {
                if (isPhasing.getOrDefault(player, false)) setPhasing(player, false);
            });
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isPhasing.getOrDefault(event.getPlayer(), false) && (isInBlock(event.getTo(), block -> impassable.contains(block.getType())))) {
            event.setCancelled(true);
        }
    }

    @Override
    public void initialize() {
        String impassableBlocks = "impassable_blocks";

        registerConfigOption(OriginsReborn.getInstance(), impassableBlocks, Collections.singletonList("Blocks you cannot pass through when phasing"), ConfigManager.SettingType.MATERIAL_LIST, List.of(
                Material.OBSIDIAN,
                Material.CRYING_OBSIDIAN,
                Material.BEDROCK
        ));

        impassable = getConfigOption(OriginsReborn.getInstance(), impassableBlocks, ConfigManager.SettingType.MATERIAL_LIST);
    }

    private List<Material> impassable;

    public boolean isInBlock(Entity entity) {
        return isInBlock(entity.getLocation(), block -> block.getType().isSolid() && !impassable.contains(block.getType()));
    }

    public boolean isInBlock(Location location, Predicate<Block> predicate) {
        boolean isInsideBlock = false;
        for (Location currentLocation : List.of(location.clone().add(0, 1, 0), location.clone())) {
            List<Double> values = List.of(0.4, -0.4);
            for (double x : values) {
                for (double z : values) {
                    if (predicate.test(currentLocation.clone().add(x, 0, z).getBlock())) {
                        isInsideBlock = true;
                        break;
                    }
                }
                if (isInsideBlock) break;
            }
            if (isInsideBlock) break;
        }
        return isInsideBlock;
    }

    private final Map<Player, Boolean> isPhasing = new HashMap<>();

    @Override
    public boolean canFly(Player player) {
        return getDependency().isEnabled(player) && isPhasing.getOrDefault(player, false);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        runForAbility(event.getEntity(), player -> {
            if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    public float getFlightSpeed(Player player) {
        return 0.1f;
    }

    @Override
    public BlockMiningContext provideContextFor(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        boolean aquaAffinity = false;
        if (helmet != null) {
            aquaAffinity = helmet.containsEnchantment(OriginsReborn.getNMSInvoker().getAquaAffinityEnchantment());
        }
        return new BlockMiningContext(
                player.getInventory().getItemInMainHand(),
                player.getPotionEffect(OriginsReborn.getNMSInvoker().getHasteEffect()),
                player.getPotionEffect(OriginsReborn.getNMSInvoker().getMiningFatigueEffect()),
                player.getPotionEffect(PotionEffectType.CONDUIT_POWER),
                OriginsReborn.getNMSInvoker().isUnderWater(player),
                aquaAffinity,
                true
        );
    }

    @Override
    public boolean shouldActivate(Player player) {
        return getDependency().isEnabled(player) && isPhasing.getOrDefault(player, false);
    }

    private void setPhasing(Player player, boolean enabled) {
        enabled = hasAbility(player) && enabled;
        Block block = player.getEyeLocation().getBlock();
        if (block.getType().isCollidable() && enabled) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, -1, 0, false, false));
        } else {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        if (isPhasing.getOrDefault(player, false) == enabled) return;
        Vector vector = player.getVelocity();
        GameMode gameMode = enabled ? GameMode.SPECTATOR : player.getGameMode();
        OriginsReborn.getNMSInvoker().sendPhasingGamemodeUpdate(player, gameMode);
        Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> player.setVelocity(vector));
        isPhasing.put(player, enabled);
    }
}

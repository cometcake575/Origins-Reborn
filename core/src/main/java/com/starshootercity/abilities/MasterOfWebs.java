package com.starshootercity.abilities;

import com.starshootercity.events.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import com.starshootercity.cooldowns.CooldownAbility;
import com.starshootercity.cooldowns.Cooldowns;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MasterOfWebs implements CooldownAbility, FlightAllowingAbility, Listener, VisibleAbility {
    private final Map<Player, List<Entity>> glowingEntities = new HashMap<>();

    private final List<Location> temporaryCobwebs = new ArrayList<>();

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (temporaryCobwebs.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
            temporaryCobwebs.remove(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        runForAbility(event.getDamager(), player -> {
            if (hasCooldown(player)) return;
            if (!event.getEntity().getLocation().getBlock().isSolid()) {
                setCooldown(player);
                Location location = event.getEntity().getLocation().getBlock().getLocation();
                temporaryCobwebs.add(location);
                location.getBlock().setType(Material.COBWEB);
                Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> {
                    if (location.getBlock().getType() == Material.COBWEB && temporaryCobwebs.contains(location)) {
                        temporaryCobwebs.remove(location);
                        location.getBlock().setType(Material.AIR);
                    }
                }, 60);
            }
        });
    }


    private void setCanFly(Player player, boolean setFly) {
        if (setFly) player.setAllowFlight(true);
        canFly.put(player, setFly);
    }

    private final Map<Player, Boolean> canFly = new HashMap<>();


    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            runForAbility(player, webMaster -> {
                if (isInCobweb(webMaster)) {
                    setCanFly(webMaster, true);
                    webMaster.setFlying(true);
                } else {
                    setCanFly(webMaster, false);
                }
                List<Entity> entities = webMaster.getNearbyEntities(16, 16, 16);
                entities.removeIf(entity -> !(entity instanceof LivingEntity));
                if (entities.size() > 16) entities = entities.subList(0, 16);
                entities.addAll(Bukkit.getOnlinePlayers());
                entities.removeIf(entity -> entity.getWorld() != webMaster.getWorld());
                entities.removeIf(entity -> entity.getLocation().distance(webMaster.getLocation()) > 16);
                for (Entity entity : entities) {
                    runForAbility(entity, null, webStuck -> {
                        if (webStuck != webMaster) {
                            if (!glowingEntities.containsKey(webMaster)) {
                                glowingEntities.put(webMaster, new ArrayList<>());
                            }
                            if (isInCobweb(webStuck)) {
                                if (!glowingEntities.get(webMaster).contains(webStuck)) {
                                    glowingEntities.get(webMaster).add(webStuck);
                                }

                                byte data = getData(webStuck);
                                OriginsReborn.getNMSInvoker().sendEntityData(webMaster, webStuck, data);
                            } else {
                                glowingEntities.get(webMaster).remove(webStuck);
                                AbilityRegister.updateEntity(webMaster, webStuck);
                            }
                        }
                    });
                }
            });
        }
    }

    private static byte getData(Entity webStuck) {
        byte data = 0x40;
        if (webStuck.getFireTicks() > 0) {
            data += 0x01;
        }
        if (webStuck instanceof LivingEntity entity) {
            if (entity.isInvisible()) data += 0x20;
        }
        if (webStuck instanceof Player stuckPlayer) {
            if (stuckPlayer.isSneaking()) {
                data += 0x02;
            }
            if (stuckPlayer.isSprinting()) {
                data += 0x08;
            }
            if (stuckPlayer.isSwimming()) {
                data += 0x10;
            }
            if (stuckPlayer.isGliding()) {
                data += (byte) 0x80;
            }
        }
        return data;
    }

    public MasterOfWebs() {
        NamespacedKey recipeKey = new NamespacedKey(OriginsReborn.getInstance(), "web-recipe");
        ShapelessRecipe webRecipe = new ShapelessRecipe(recipeKey, new ItemStack(Material.COBWEB));
        if (Bukkit.getRecipe(recipeKey) == null) {
            webRecipe.addIngredient(Material.STRING);
            webRecipe.addIngredient(Material.STRING);
            Bukkit.addRecipe(webRecipe);
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() != null) {
            if (event.getRecipe().getResult().getType() == Material.COBWEB) {
                for (HumanEntity entity : event.getInventory().getViewers()) {
                    runForAbility(entity, null, player -> event.getInventory().setResult(null));
                }
            }
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:master_of_webs");
    }

    @Override
    public String description() {
        return "You navigate cobweb perfectly, and are able to climb in them. When you hit an enemy in melee, they get stuck in cobweb for a while. Non-arthropods stuck in cobweb will be sensed by you. You are able to craft cobweb from string.";
    }

    @Override
    public String title() {
        return "Master of Webs";
    }

    public boolean isInCobweb(Entity entity) {
        for (Block start : Set.of(entity.getLocation().getBlock().getRelative(BlockFace.UP), entity.getLocation().getBlock())) {
            if (start.getType() == Material.COBWEB) return true;
            for (BlockFace face : BlockFace.values()) {
                Block block = start.getRelative(face);
                if (block.getType() != Material.COBWEB) continue;
                if (entity.getBoundingBox().overlaps(block.getBoundingBox())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canFly(Player player) {
        return canFly.getOrDefault(player, false);
    }

    @Override
    public float getFlightSpeed(Player player) {
        return 0.04f;
    }

    @Override
    public Cooldowns.CooldownInfo getCooldownInfo() {
        return new Cooldowns.CooldownInfo(120, "web");
    }
}

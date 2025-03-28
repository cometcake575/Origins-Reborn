package com.starshootercity.abilities;

import com.starshootercity.OriginsReborn;
import com.starshootercity.abilities.types.VisibleAbility;
import com.starshootercity.abilities.types.CooldownAbility;
import com.starshootercity.cooldowns.Cooldowns;
import com.starshootercity.events.PlayerLeftClickEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ThrowEnderPearl implements Listener, CooldownAbility, VisibleAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:throw_ender_pearl");
    }

    @Override
    public String description() {
        return "Whenever you want, you may throw an ender pearl which deals no damage, allowing you to teleport.";
    }

    @Override
    public String title() {
        return "Teleportation";
    }

    private final NamespacedKey falseEnderPearlKey = new NamespacedKey(OriginsReborn.getInstance(), "false-ender-pearl");

    @EventHandler
    public void onPlayerLeftClick(PlayerLeftClickEvent event) {
        if (event.hasBlock()) return;
        runForAbility(event.getPlayer(), player -> {
            if (player.getTargetBlock(6) != null) return;
            if (player.getInventory().getItemInMainHand().getType() != Material.AIR) return;
            if (hasCooldown(player)) return;
            setCooldown(player);
            Projectile projectile = player.launchProjectile(EnderPearl.class);
            projectile.getPersistentDataContainer().set(falseEnderPearlKey, PersistentDataType.STRING, player.getName());
        });
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(falseEnderPearlKey, PersistentDataType.STRING)) {
            event.setCancelled(true);
            String name = event.getEntity().getPersistentDataContainer().get(falseEnderPearlKey, PersistentDataType.STRING);
            if (name == null) return;
            Player player = Bukkit.getPlayer(name);
            if (player == null) return;
            Location loc = event.getEntity().getLocation();
            loc.setPitch(player.getLocation().getPitch());
            loc.setYaw(player.getLocation().getYaw());
            player.setFallDistance(0);
            player.setVelocity(new Vector());
            player.teleport(loc);
            event.getEntity().remove();
        }
    }

    @Override
    public Cooldowns.CooldownInfo getCooldownInfo() {
        return new Cooldowns.CooldownInfo(30, "ender_pearl");
    }
}

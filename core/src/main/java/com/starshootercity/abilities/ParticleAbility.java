package com.starshootercity.abilities;

import com.starshootercity.events.ServerTickEndEvent;
import com.starshootercity.Origin;
import com.starshootercity.OriginSwapper;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public interface ParticleAbility extends Ability {
    Particle getParticle();
    default int getFrequency() {
        return 4;
    }
    default int getExtra() {
        return 0;
    }
    default Object getData() {
        return null;
    }

    class ParticleAbilityListener implements Listener {
        @EventHandler
        public void onServerTickEnd(ServerTickEndEvent event) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                List<Origin> origins = OriginSwapper.getOrigins(player);
                List<Ability> abilities = new ArrayList<>();
                for (Origin origin : origins) abilities.addAll(origin.getAbilities());
                for (Ability ability : abilities) {
                    if (ability instanceof ParticleAbility particleAbility) {
                        if (event.getTickNumber() % particleAbility.getFrequency() == 0) {
                            player.getWorld().spawnParticle(particleAbility.getParticle(), player.getLocation(), 1, 0.5, 1, 0.5, particleAbility.getExtra(), particleAbility.getData());
                        }
                    }
                }
            }
        }
    }
}

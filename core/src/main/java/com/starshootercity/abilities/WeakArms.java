package com.starshootercity.abilities;

import com.starshootercity.events.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import com.starshootercity.SavedPotionEffect;
import com.starshootercity.util.ShortcutUtils;
import com.starshootercity.util.config.ConfigManager;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WeakArms implements Listener, VisibleAbility {

    private List<Material> naturalStones;

    @Override
    public void initialize() {
        String naturalStone = "natural_stones";
        registerConfigOption(OriginsReborn.getInstance(), naturalStone, Collections.singletonList("Blocks that count as natural stone"), ConfigManager.SettingType.MATERIAL_LIST, List.of(
                Material.STONE,
                Material.TUFF,
                Material.GRANITE,
                Material.DIORITE,
                Material.ANDESITE,
                Material.SANDSTONE,
                Material.SMOOTH_SANDSTONE,
                Material.RED_SANDSTONE,
                Material.SMOOTH_RED_SANDSTONE,
                Material.DEEPSLATE,
                Material.BLACKSTONE,
                Material.NETHERRACK
        ));

        naturalStones = getConfigOption(OriginsReborn.getInstance(), naturalStone, ConfigManager.SettingType.MATERIAL_LIST);
    }

    private final Map<Player, SavedPotionEffect> storedEffects = new HashMap<>();

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        Attribute attribute = OriginsReborn.getNMSInvoker().getBlockBreakSpeedAttribute();
        for (Player p : Bukkit.getOnlinePlayers()) {
            runForAbility(p, player -> {
                Block target = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
                PotionEffect strength = player.getPotionEffect(OriginsReborn.getNMSInvoker().getStrengthEffect());
                int sides = 0;
                if (target != null && naturalStones.contains(target.getType())) {
                    if (naturalStones.contains(target.getRelative(BlockFace.DOWN).getType())) sides++;
                    if (naturalStones.contains(target.getRelative(BlockFace.UP).getType())) sides++;
                    if (naturalStones.contains(target.getRelative(BlockFace.WEST).getType())) sides++;
                    if (naturalStones.contains(target.getRelative(BlockFace.EAST).getType())) sides++;
                    if (naturalStones.contains(target.getRelative(BlockFace.NORTH).getType())) sides++;
                    if (naturalStones.contains(target.getRelative(BlockFace.SOUTH).getType())) sides++;
                }
                if (sides > 2 && strength == null && naturalStones.contains(target.getType())) {
                    if (attribute == null) {
                        PotionEffect effect = player.getPotionEffect(OriginsReborn.getNMSInvoker().getMiningFatigueEffect());
                        boolean ambient = false;
                        boolean showParticles = false;
                        if (effect != null) {
                            ambient = effect.isAmbient();
                            showParticles = effect.hasParticles();
                            if (effect.getAmplifier() != -1) {
                                storedEffects.put(player, new SavedPotionEffect(effect, ShortcutUtils.getCurrentTick()));
                                player.removePotionEffect(OriginsReborn.getNMSInvoker().getMiningFatigueEffect());
                            }
                        }
                        player.addPotionEffect(new PotionEffect(OriginsReborn.getNMSInvoker().getMiningFatigueEffect(), ShortcutUtils.infiniteDuration(), -1, ambient, showParticles));
                    } else {
                        AttributeInstance instance = player.getAttribute(attribute);
                        if (instance == null) return;
                        if (OriginsReborn.getNMSInvoker().getAttributeModifier(instance, key) == null) {
                            OriginsReborn.getNMSInvoker().addAttributeModifier(instance, key, "weak-arms", -1, AttributeModifier.Operation.ADD_NUMBER);
                        }
                    }
                } else {
                    if (attribute == null) {
                        if (player.hasPotionEffect(OriginsReborn.getNMSInvoker().getMiningFatigueEffect())) {
                            PotionEffect effect = player.getPotionEffect(OriginsReborn.getNMSInvoker().getMiningFatigueEffect());
                            if (effect != null) {
                                if (effect.getAmplifier() == -1)
                                    player.removePotionEffect(OriginsReborn.getNMSInvoker().getMiningFatigueEffect());
                            }
                        }
                        if (storedEffects.containsKey(player)) {
                            SavedPotionEffect effect = storedEffects.get(player);
                            storedEffects.remove(player);
                            PotionEffect potionEffect = effect.effect();
                            int time = potionEffect.getDuration() - (ShortcutUtils.getCurrentTick() - effect.currentTime());
                            if (time > 0) {
                                player.addPotionEffect(new PotionEffect(
                                        potionEffect.getType(),
                                        time,
                                        potionEffect.getAmplifier(),
                                        potionEffect.isAmbient(),
                                        potionEffect.hasParticles()
                                ));
                            }
                        }
                    } else {
                        AttributeInstance instance = player.getAttribute(attribute);
                        if (instance == null) return;
                        AttributeModifier attributeModifier = OriginsReborn.getNMSInvoker().getAttributeModifier(instance, key);
                        if (attributeModifier == null) return;
                        instance.removeModifier(attributeModifier);
                    }
                }
            });
        }
    }

    private final NamespacedKey key = new NamespacedKey(OriginsReborn.getInstance(), "break-speed-modifier");

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            storedEffects.remove(event.getPlayer());
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:weak_arms");
    }

    @Override
    public String description() {
        return "When not under the effect of a strength potion, you can only mine natural stone if there are at most 2 other natural stone blocks adjacent to it.";
    }

    @Override
    public String title() {
        return "Weak Arms";
    }

}

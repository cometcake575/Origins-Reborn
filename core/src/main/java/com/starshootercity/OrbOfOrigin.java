package com.starshootercity;

import com.starshootercity.events.PlayerSwapOriginEvent;
import com.starshootercity.util.ShortcutUtils;
import com.starshootercity.util.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class OrbOfOrigin implements Listener {
    public static NamespacedKey orbKey = new NamespacedKey(OriginsReborn.getInstance(), "orb-of-origin");
    public static NamespacedKey updatedKey = new NamespacedKey(OriginsReborn.getInstance(), "updated-orb");

    public static ItemStack orb;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta.getPersistentDataContainer().has(orbKey, OriginSwapper.BooleanPDT.BOOLEAN) && !meta.getPersistentDataContainer().has(updatedKey, OriginSwapper.BooleanPDT.BOOLEAN)) {
            event.getCurrentItem().setItemMeta(orb.getItemMeta());
        }
    }

    @EventHandler
    @SuppressWarnings("ConstantConditions") // itemStack is null on some versions
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe != null) {
            if (recipe.getResult().getType() == Material.CONDUIT) {
                for (ItemStack itemStack : event.getInventory().getMatrix()) {
                    if (itemStack != null && itemStack.getItemMeta() != null) {
                        if (itemStack.getItemMeta().getPersistentDataContainer().has(orbKey, OriginSwapper.BooleanPDT.BOOLEAN)) {
                            event.getInventory().setResult(null);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public OrbOfOrigin() {
        orb = new ItemStack(Material.NAUTILUS_SHELL);

        String orbOfOrigin = "item.orb_of_origin";

        Translator.registerTranslation(orbOfOrigin, "Orb of Origin");
        ItemMeta meta = orb.getItemMeta();
        meta.getPersistentDataContainer().set(orbKey, OriginSwapper.BooleanPDT.BOOLEAN, true);
        meta.getPersistentDataContainer().set(updatedKey, OriginSwapper.BooleanPDT.BOOLEAN, true);
        meta = OriginsReborn.getNMSInvoker().setCustomModelData(meta, 1);
        meta.displayName(
                Component.text(Translator.translate(orbOfOrigin))
                        .color(NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false)
        );
        orb.setItemMeta(meta);


        Bukkit.removeRecipe(orbKey);
        FileConfiguration config = OriginsReborn.getInstance().getConfig();
        if (config.getBoolean("orb-of-origin.enable-recipe")) {
            ShapedRecipe shapedRecipe = new ShapedRecipe(orbKey, orb);
            shapedRecipe.shape("012", "345", "678");
            int i = 0;
            List<?> recipeData = config.getList("orb-of-origin.recipe");
            if (recipeData == null) return;
            for (Object line : recipeData) {
                for (String name : (List<String>) line) {
                    Material material = Material.matchMaterial(name);
                    if (material == null) material = Material.AIR;
                    if (material == Material.AIR) {
                        i++;
                        continue;
                    }
                    shapedRecipe.setIngredient(String.valueOf(i).charAt(0), material);
                    i++;
                }
            }
            Bukkit.addRecipe(shapedRecipe);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType().isInteractable()) return;
        }
        if (!ShortcutUtils.RIGHT_CLICK.contains(event.getAction())) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.getPersistentDataContainer().has(orbKey, OriginSwapper.BooleanPDT.BOOLEAN)) {
                if (OriginSwapper.orbCooldown.containsKey(event.getPlayer())) {
                    if (System.currentTimeMillis() - OriginSwapper.orbCooldown.get(event.getPlayer()) < 500) {
                        return;
                    }
                }
                ItemMeta heldMeta = event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
                EquipmentSlot hand = EquipmentSlot.OFF_HAND;
                if (heldMeta != null && heldMeta.getPersistentDataContainer().has(orbKey, OriginSwapper.BooleanPDT.BOOLEAN)) hand = EquipmentSlot.HAND;
                if (hand == EquipmentSlot.HAND) event.getPlayer().swingMainHand();
                else event.getPlayer().swingOffHand();
                if (ConfigManager.getConfigValue(ConfigManager.Option.ORB_OF_ORIGIN_CONSUME)) {
                    item.setAmount(item.getAmount() - 1);
                    event.getPlayer().getInventory().setItemInMainHand(item);
                }
                boolean opened = false;
                for (String layer : AddonLoader.layers) {
                    OriginSwapper.setOrigin(event.getPlayer(), null, PlayerSwapOriginEvent.SwapReason.ORB_OF_ORIGIN, false, layer);
                    if (opened) continue;
                    if (OriginsReborn.getInstance().getConfig().getBoolean("orb-of-origin.random.%s".formatted(layer))) {
                        OriginSwapper.selectRandomOrigin(event.getPlayer(), PlayerSwapOriginEvent.SwapReason.ORB_OF_ORIGIN, layer);
                        OriginSwapper.openOriginSwapper(event.getPlayer(), PlayerSwapOriginEvent.SwapReason.ORB_OF_ORIGIN, AddonLoader.getOrigins(layer).indexOf(OriginSwapper.getOrigin(event.getPlayer(), layer)), 0, false, true, layer);
                    } else {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> OriginSwapper.openOriginSwapper(event.getPlayer(), PlayerSwapOriginEvent.SwapReason.ORB_OF_ORIGIN, 0, 0, layer));
                    }
                    opened = true;
                }
            }
        }
    }
}

package me.kalbskinder.patientZero.utils;

import me.kalbskinder.patientZero.PatientZero;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ItemMaker {
    private static final Logger logger = Logger.getLogger("PTZ");
    private static final NamespacedKey ACTION_KEY = new NamespacedKey(PatientZero.getPlugin(PatientZero.class), "item_action");

    /**
     * Creates an ItemStack with the specified properties and optional right-click action.
     *
     * @param item     The material identifier (e.g., "minecraft:cobblestone").
     * @param amount   The quantity of the item.
     * @param itemName The display name of the item (supports color codes with &).
     * @param lore     The lore lines for the item (supports color codes with &).
     * @param actionId A unique identifier for the right-click action (null if no action).
     * @return The configured ItemStack, or null if the material is invalid.
     */
    public static ItemStack createItem(String item, int amount, String itemName, List<String> lore, String actionId) {

        // Validate input
        if (item == null || item.trim().isEmpty()) {
            logger.warning("Invalid item identifier: " + item);
            return null;
        }

        if (amount < 1) {
            logger.warning("Invalid amount: " + amount + ". Setting to 1.");
            amount = 1;
        }

        // Parse material
        String materialName = item.toUpperCase().replace("MINECRAFT:", "");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            logger.warning("Invalid material: " + item);
            return null;
        }

        // Create ItemStack
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            logger.warning("Failed to get ItemMeta for material: " + materialName);
            return itemStack;
        }

        // Set display name
        if (itemName != null && !itemName.trim().isEmpty()) {
            meta.setDisplayName(itemName);
        }

        // Set lore
        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = lore.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            meta.setLore(coloredLore);
        }

        // Set action identifier
        if (actionId != null && !actionId.trim().isEmpty()) {
            meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, actionId);
        }

        // Make item not lose durability
        meta.setUnbreakable(true);

        // Apply meta
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Gives the item to a player in the specified inventory slot.
     *
     * @param player The player to receive the item.
     * @param item   The ItemStack to give.
     * @param slot   The inventory slot (0-35 for main inventory, 36-39 for armor, 40 for off-hand).
     */
    public static void giveItemToPlayer(Player player, ItemStack item, int slot) {
        if (player == null || !player.isOnline()) {
            logger.warning("Cannot give item to null or offline player");
            return;
        }
        if (item == null) {
            logger.warning("Cannot give null item to player: " + player.getName());
            return;
        }

        // Available inventory slots of a players GUI
        if (slot < 0 || slot > 40) {
            logger.warning("Invalid inventory slot: " + slot + " for player: " + player.getName());
            return;
        }

        // Handle main inventory (0-35), armor (36-39), or off-hand (40)
        if (slot <= 35) {
            player.getInventory().setItem(slot, item);
        } else if (slot == 40) {
            player.getInventory().setItemInOffHand(item);
        } else {
            // Armor slots (36: helmet, 37: chestplate, 38: leggings, 39: boots)
            switch (slot) {
                case 39:
                    player.getInventory().setHelmet(item);
                    break;
                case 38:
                    player.getInventory().setChestplate(item);
                    break;
                case 37:
                    player.getInventory().setLeggings(item);
                    break;
                case 36:
                    player.getInventory().setBoots(item);
                    break;
                default:
                    return;
            }
        }

        player.updateInventory(); // Update the inventory changes
    }

    /**
     * Gets the action identifier from an ItemStack.
     *
     * @param item The ItemStack to check.
     * @return The action identifier, or null if none.
     */
    public static String getActionId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(ACTION_KEY, PersistentDataType.STRING);
    }
}
package me.kalbskinder.patientZero.systems;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.enums.PlayerRole;
import me.kalbskinder.patientZero.utils.ItemMaker;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemDistributor {
    public static FileConfiguration config;

    private static List<String> emptyLore = new ArrayList<String>();

    private static ItemStack survivorBow;
    private static ItemStack survivorArrow;

    private static ItemStack corruptedSword;
    private static ItemStack corruptedHelmet;
    private static ItemStack corruptedLeggings;
    private static ItemStack corruptedBoots;

    private static ItemStack patientzeroBow;
    private static ItemStack patientZeroSword;

    // Get all items from the config
    public static void register(PatientZero plugin) {
        config = plugin.getConfig();

        // Read the items from the config for each role
        // Survivors
        survivorBow = ItemMaker.createItem(
                "minecraft:bow",
                1,
                config.getString("settings.game-items.survivor.bow.name"),
                config.getStringList("settings.game-items.survivor.bow.lore"),
                ""
        );

        survivorArrow = ItemMaker.createItem(
                "minecraft:arrow",
                config.getInt("settings.game-items.survivor.arrows.amount"),
                config.getString("settings.game-items.survivor.arrows.name"),
                config.getStringList("settings.game-items.survivor.arrows.lore"),
                ""
        );

        // Corrupted
        corruptedSword = ItemMaker.createItem(
                config.getString("settings.game-items.corrupted.sword.type"),
                1,
                config.getString("settings.game-items.corrupted.sword.name"),
                config.getStringList("settings.game-items.corrupted.sword.lore"),
                ""
        );

        corruptedHelmet = ItemMaker.createItem(
                config.getString("settings.game-items.corrupted.armor.helmet.type"),
                1,
                config.getString("settings.game-items.corrupted.armor.helmet.name"),
                emptyLore,
                ""
        );

        corruptedLeggings = ItemMaker.createItem(
                config.getString("settings.game-items.corrupted.armor.leggings.type"),
                1,
                config.getString("settings.game-items.corrupted.armor.leggings.name"),
                emptyLore,
                ""
        );

        corruptedBoots = ItemMaker.createItem(
                config.getString("settings.game-items.corrupted.armor.boots.type"),
                1,
                config.getString("settings.game-items.corrupted.armor.boots.name"),
                emptyLore,
                ""
        );

        // Patient-Zero
        patientzeroBow = ItemMaker.createItem(
                "minecraft:bow",
                1,
                config.getString("settings.game-items.patientzero.bow.name"),
                config.getStringList("settings.game-items.patientzero.bow.lore"),
                ""
        );

        patientZeroSword = ItemMaker.createItem(
                config.getString("settings.game-items.patientzero.sword.type"),
                1,
                config.getString("settings.game-items.patientzero.sword.name"),
                config.getStringList("settings.game-items.patientzero.sword.lore"),
                ""
        );
    }

    // Give each role their items
    public static void applyRoleLayout(Player player, PlayerRole role) {
        Inventory inventory = player.getInventory();
        inventory.clear();

        if (role == PlayerRole.SURVIVOR) {
            ItemMaker.giveItemToPlayer(player, survivorBow, 0);
            ItemMaker.giveItemToPlayer(player, survivorArrow, 9);

        } else if (role == PlayerRole.CORRUPTED) {
            applyCorruptedLayout(player);

        } else if (role == PlayerRole.PATIENT_ZERO) {
            ItemMaker.giveItemToPlayer(player, patientzeroBow, 0);
            ItemMaker.giveItemToPlayer(player, patientZeroSword, 2);
            player.getInventory().setHeldItemSlot(1); // Select an empty slot so that the patient-zero isn't holding an item
        }
    }

    // Apply corrupted layout (needs to be applied on the start of the game and after respawning)
    public static void applyCorruptedLayout(Player player) {
        ItemMaker.giveItemToPlayer(player, corruptedSword, 0);
        ItemMaker.giveItemToPlayer(player, corruptedHelmet, 39);
        ItemMaker.giveItemToPlayer(player, corruptedLeggings, 37);
        ItemMaker.giveItemToPlayer(player, corruptedBoots, 36);
    }

    public static ItemStack getCorruptedSword() {
        return corruptedSword;
    }

    public static ItemStack getPatientZeroSword() {
        return patientZeroSword;
    }
}

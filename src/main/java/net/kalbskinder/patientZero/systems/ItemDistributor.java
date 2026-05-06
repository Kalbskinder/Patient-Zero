package net.kalbskinder.patientZero.systems;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kalbskinder.patientZero.PatientZero;
import net.kalbskinder.patientZero.enums.PlayerRole;
import net.kalbskinder.patientZero.utils.ItemMaker;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemDistributor {
    private final ItemMaker itemMaker;

    private final ItemStack survivorBow;
    private final ItemStack survivorArrow;

    @Getter private final ItemStack corruptedSword;
    private final ItemStack corruptedHelmet;
    private final ItemStack corruptedLeggings;
    private final ItemStack corruptedBoots;

    private final ItemStack patientzeroBow;
    @Getter private final ItemStack patientZeroSword;

    // Get all items from the config
    public ItemDistributor(PatientZero plugin, ItemMaker itemMaker) {
        this.itemMaker = itemMaker;

        FileConfiguration config = plugin.getConfig();
        List<String> emptyLore = new ArrayList<>();

        // Read the items from the config for each role
        // Survivors
        survivorBow = itemMaker.createItem(
                "minecraft:bow",
                1,
                config.getString("settings.game-items.survivor.bow.name"),
                config.getStringList("settings.game-items.survivor.bow.lore"),
                ""
        );

        survivorArrow = itemMaker.createItem(
                "minecraft:arrow",
                config.getInt("settings.game-items.survivor.arrows.amount"),
                config.getString("settings.game-items.survivor.arrows.name"),
                config.getStringList("settings.game-items.survivor.arrows.lore"),
                ""
        );

        // Corrupted
        corruptedSword = itemMaker.createItem(
                config.getString("settings.game-items.corrupted.sword.type"),
                1,
                config.getString("settings.game-items.corrupted.sword.name"),
                config.getStringList("settings.game-items.corrupted.sword.lore"),
                ""
        );

        corruptedHelmet = itemMaker.createItem(
                config.getString("settings.game-items.corrupted.armor.helmet.type"),
                1,
                config.getString("settings.game-items.corrupted.armor.helmet.name"),
                emptyLore,
                ""
        );

        corruptedLeggings = itemMaker.createItem(
                config.getString("settings.game-items.corrupted.armor.leggings.type"),
                1,
                config.getString("settings.game-items.corrupted.armor.leggings.name"),
                emptyLore,
                ""
        );

        corruptedBoots = itemMaker.createItem(
                config.getString("settings.game-items.corrupted.armor.boots.type"),
                1,
                config.getString("settings.game-items.corrupted.armor.boots.name"),
                emptyLore,
                ""
        );

        // Patient-Zero
        patientzeroBow = itemMaker.createItem(
                "minecraft:bow",
                1,
                config.getString("settings.game-items.patientzero.bow.name"),
                config.getStringList("settings.game-items.patientzero.bow.lore"),
                ""
        );

        patientZeroSword = itemMaker.createItem(
                config.getString("settings.game-items.patientzero.sword.type"),
                1,
                config.getString("settings.game-items.patientzero.sword.name"),
                config.getStringList("settings.game-items.patientzero.sword.lore"),
                ""
        );
    }

    // Give each role their items
    public void applyRoleLayout(Player player, PlayerRole role) {
        Inventory inventory = player.getInventory();
        inventory.clear();

        if (role == PlayerRole.SURVIVOR) {
            itemMaker.giveItemToPlayer(player, survivorBow, 0);
            itemMaker.giveItemToPlayer(player, survivorArrow, 9);

        } else if (role == PlayerRole.CORRUPTED) {
            applyCorruptedLayout(player);

        } else if (role == PlayerRole.PATIENT_ZERO) {
            itemMaker.giveItemToPlayer(player, patientzeroBow, 0);
            itemMaker.giveItemToPlayer(player, patientZeroSword, 2);
            player.getInventory().setHeldItemSlot(1); // Select an empty slot so that the patient-zero isn't holding an item
        }
    }

    // Apply corrupted layout (needs to be applied on the start of the game and after respawning)
    public void applyCorruptedLayout(Player player) {
        itemMaker.giveItemToPlayer(player, corruptedSword, 0);
        itemMaker.giveItemToPlayer(player, corruptedHelmet, 39);
        itemMaker.giveItemToPlayer(player, corruptedLeggings, 37);
        itemMaker.giveItemToPlayer(player, corruptedBoots, 36);
    }
}

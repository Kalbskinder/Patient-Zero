package net.kalbskinder.infection.systems;

import lombok.Getter;
import net.kalbskinder.infection.Infection;
import net.kalbskinder.infection.enums.PlayerRole;
import net.kalbskinder.infection.utils.ItemMaker;
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

    @Getter private final ItemStack infectedSword;
    private final ItemStack infectedHelmet;
    private final ItemStack infectedLeggings;
    private final ItemStack infectedBoots;

    private final ItemStack alphaBow;
    @Getter private final ItemStack alphaSword;

    // Get all items from the config
    public ItemDistributor(Infection plugin, ItemMaker itemMaker) {
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

        // Infected
        infectedSword = itemMaker.createItem(
                config.getString("settings.game-items.infected.sword.type"),
                1,
                config.getString("settings.game-items.infected.sword.name"),
                config.getStringList("settings.game-items.infected.sword.lore"),
                ""
        );

        infectedHelmet = itemMaker.createItem(
                config.getString("settings.game-items.infected.armor.helmet.type"),
                1,
                config.getString("settings.game-items.infected.armor.helmet.name"),
                emptyLore,
                ""
        );

        infectedLeggings = itemMaker.createItem(
                config.getString("settings.game-items.infected.armor.leggings.type"),
                1,
                config.getString("settings.game-items.infected.armor.leggings.name"),
                emptyLore,
                ""
        );

        infectedBoots = itemMaker.createItem(
                config.getString("settings.game-items.infected.armor.boots.type"),
                1,
                config.getString("settings.game-items.infected.armor.boots.name"),
                emptyLore,
                ""
        );

        // Alpha
        alphaBow = itemMaker.createItem(
                "minecraft:bow",
                1,
                config.getString("settings.game-items.alpha.bow.name"),
                config.getStringList("settings.game-items.alpha.bow.lore"),
                ""
        );

        alphaSword = itemMaker.createItem(
                config.getString("settings.game-items.alpha.sword.type"),
                1,
                config.getString("settings.game-items.alpha.sword.name"),
                config.getStringList("settings.game-items.alpha.sword.lore"),
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

        } else if (role == PlayerRole.INFECTED) {
            applyInfectedLayout(player);

        } else if (role == PlayerRole.ALPHA) {
            itemMaker.giveItemToPlayer(player, alphaBow, 0);
            itemMaker.giveItemToPlayer(player, alphaSword, 2);
            player.getInventory().setHeldItemSlot(1); // Select an empty slot so that the alpha isn't holding an item
        }
    }

    // Apply infected layout (needs to be applied on the start of the game and after respawning)
    public void applyInfectedLayout(Player player) {
        itemMaker.giveItemToPlayer(player, infectedSword, 0);
        itemMaker.giveItemToPlayer(player, infectedHelmet, 39);
        itemMaker.giveItemToPlayer(player, infectedLeggings, 37);
        itemMaker.giveItemToPlayer(player, infectedBoots, 36);
    }
}

package net.kalbskinder.infection.commands;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.helpers.commands.CommandHelper;
import net.kalbskinder.helpers.commands.CommandManager;
import net.kalbskinder.infection.Infection;
import net.kalbskinder.infection.systems.LocationSelection;
import net.kalbskinder.infection.systems.Queue;
import net.kalbskinder.infection.utils.MMUtils;
import net.kalbskinder.infection.utils.Prefixes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public class BaseCommand {
    private static final String PREFIX = Prefixes.getPrefix();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private final List<CommandHelper> commands = new ArrayList<>();
    private final List<String> mapNameSuggestions = new ArrayList<>();
    private final List<String> roleSuggestions = List.of("infected", "survivor");
    private final List<String> helpSuggestions = List.of("guide", "commands");

    private final CreateMapCommand createMapCommand;
    private final DeleteMapCommand deleteMapCommand;
    private final JoinLeaveCommand joinLeaveCommand;
    private final ListCommands listCommands;
    private final MapSpawnsCommand mapSpawnsCommand;
    private final CommandManager commandManager;

    private final Infection plugin;
    private final Queue queue;
    private final LocationSelection locationSelection;

    public void register() {
        commands.clear();
        mapNameSuggestions.clear();

        ConfigurationSection maps = plugin.getConfig().getConfigurationSection("maps");
        if (maps != null) {
            mapNameSuggestions.addAll(maps.getKeys(false));
        }

        commands.add(CommandHelper.create("infection")
                .sub("help").executes(ctx -> executeHelp(ctx.getSender(), "unknown")).end()
                .sub("help").customArg("help-option", helpSuggestions).executes(ctx -> executeHelp(ctx.getSender(), ctx.getCustomArg("help-option"))).end()
                .sub("createmap").stringArg("map-name").executes(ctx -> executeCreateMap(ctx.getSender(), ctx.getString("map-name"))).end()
                .sub("pos1").executes(ctx -> executeSetPos1(ctx.getSender())).end()
                .sub("pos2").executes(ctx -> executeSetPos2(ctx.getSender())).end()
                .sub("wand").executes(ctx -> executeWand(ctx.getSender())).end()
                .sub("discardSelection").executes(ctx -> executeDiscardSelection(ctx.getSender())).end()
                .sub("list").executes(ctx -> executeListMaps(ctx.getSender())).end()
                .sub("deletemap").customArg("map-name", mapNameSuggestions).executes(ctx -> executeDeleteMap(ctx.getSender(), ctx.getString("map-name"))).end()
                .sub("addspawn").customArg("map-name", mapNameSuggestions).customArg("role", roleSuggestions).executes(ctx -> executeAddSpawn(ctx.getSender(), ctx.getString("role"), ctx.getString("map-name"))).end()
                .sub("setqueue-spawn").customArg("map-name", mapNameSuggestions).executes(ctx -> executeSetQueueSpawn(ctx.getSender(), ctx.getString("map-name"))).end()
                .sub("setqueue-limit").customArg("map-name", mapNameSuggestions).intArg("limit").executes(ctx -> executeSetQueueLimit(ctx.getSender(), ctx.getString("map-name"), ctx.getInt("limit"))).end()
                .sub("join").customArg("map-name", mapNameSuggestions).executes(ctx -> executeJoin(ctx.getSender(), ctx.getString("map-name"))).end()
                .sub("leave").executes(ctx -> executeLeave(ctx.getSender())).end()
                .executes(ctx -> executeBase(ctx.getSender()))
        );

        commandManager.registerCommands(commands);
    }

    private void executeBase(CommandSender sender) {
        Player player = verifyAdmin(sender);
        if (player == null) return;

        MMUtils.sendMessage(player, PREFIX + "<gray>Use <yellow>/infection help <gray>for a list of commands.<reset>");
    }

    private void executeHelp(CommandSender sender, String helpOption) {
        Player player = verifyAdmin(sender);
        if (player == null) return;

        if (helpOption.equals("guide")) {
            MMUtils.sendMessage(player, PREFIX + "<green>Step by step setup guide:");
            MMUtils.sendMessage(player, "<gray>1. <white>Use <yellow>/infection wand<white> to get the area selection wand.");
            MMUtils.sendMessage(player, "<gray>2. <white>Use the <light_purple>Selection Wand <white>to select a map area.");
            MMUtils.sendMessage(player, "<gray>3. <white>Create a map using <yellow>/infection createmap <gray><map-name><white>.");
            MMUtils.sendMessage(player, "<gray>4. <white>Set a queue spawn");
            MMUtils.sendMessage(player, "<gray>   <white>using <yellow>/infection setqueue-spawn <gray><map-name><white>.");
            MMUtils.sendMessage(player, "<gray>5. <white>Optionally change the maximum queue size");
            MMUtils.sendMessage(player, "<gray>   <white>using <yellow>/infection setqueue-limit <gray><map-name> <limit><white>.");
            MMUtils.sendMessage(player, "<gray>6. <white>Set the spawnpoints where survivors/infected players");
            MMUtils.sendMessage(player, "<gray>   <white>will spawn using <yellow>/infection addspawn <gray><map-name> <role><white>.");
            MMUtils.sendMessage(player, "<white>You can now join the map using <yellow>/infection join <gray><map-name>");
        } else if (helpOption.equals("commands")) {
            MMUtils.sendMessage(player, PREFIX + "<green>Available commands:<reset>");
            MMUtils.sendMessage(player, "<gray>- <yellow>/infection <gold>createmap <gray><map-name>");
            MMUtils.sendMessage(player, "<gray>- <yellow>/infection <gold>pos1");
            MMUtils.sendMessage(player, "<gray>- <yellow>/infection <gold>pos2");
            MMUtils.sendMessage(player, "<gray>- <yellow>/infection <gold>wand");
            MMUtils.sendMessage(player, "<gray>- <yellow>/infection <gold>discardSelection");
            MMUtils.sendMessage(player, "<gray>- <yellow>/infection <gold>deletemap <gray><map-name>");
            MMUtils.sendMessage(player, "<gray>- <yellow>/infection <gold>addspawn <gray><map-name> <role>");
            MMUtils.sendMessage(player, "<gray>- <yellow>/infection <gold>setqueue-spawn <gray><map-name>");
            MMUtils.sendMessage(player, "<gray>- <yellow>/infection <gold>setqueue-limit <gray><map-name> <int-limit>");
            MMUtils.sendMessage(player, "<gray>- <yellow>/infection <gold>list");
            MMUtils.sendMessage(player, "");
            MMUtils.sendMessage(player, "<gray>- <yellow>/infection <gold>join <gray><map-name>");
            MMUtils.sendMessage(player, "<gray>- <yellow>/infection <gold>leave");
        } else {
            MMUtils.sendMessage(player, PREFIX + "<red>Unknown help topic.");
            MMUtils.sendMessage(player, PREFIX + "<red>Available topics: <yellow>guide<red>, <yellow>commands");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
        }
    }

    private void executeCreateMap(CommandSender sender, String mapName) {
        Player player = verifyAdmin(sender);
        if (player == null) return;

        Location pos1 = locationSelection.getPos1();
        Location pos2 = locationSelection.getPos2();

        if (pos1 == null || pos2 == null) {
            MMUtils.sendMessage(player, PREFIX + "<red>You need to set both positions first using '/infection pos1' and '/infection pos2'.");
            return;
        }

        mapNameSuggestions.add(mapName);
        createMapCommand.createMap(mapName, pos1, pos2, player, plugin);
    }

    private void executeListMaps(CommandSender sender) {
        Player player = verifyAdmin(sender);
        if (player == null) return;

        listCommands.listMaps(sender, player, plugin);
    }

    private void executeDeleteMap(CommandSender sender, String mapName) {
        Player player = verifyAdmin(sender);
        if (player == null) return;

        mapNameSuggestions.remove(mapName);
        deleteMapCommand.deleteMap(sender, mapName, player, plugin);
    }

    private void executeAddSpawn(CommandSender sender, String role, String mapName) {
        Player player = verifyAdmin(sender);
        if (player == null) return;
        if (!role.equalsIgnoreCase("infected") && !role.equalsIgnoreCase("survivor")) {
            MMUtils.sendMessage(player, PREFIX + "<red><role> has to be \"infected\" or \"survivor\".");
            return;
        }

        mapSpawnsCommand.addMapSpawns(sender, mapName, role.toLowerCase(), player, plugin);
    }

    private void executeSetQueueSpawn(CommandSender sender, String mapName) {
        Player player = verifyAdmin(sender);
        if (player == null) return;

        mapSpawnsCommand.setQueueSpawn(sender, mapName, player, plugin);
    }

    private void executeSetQueueLimit(CommandSender sender, String mapName, int limit) {
        Player player = verifyAdmin(sender);
        if (player == null) return;

        queue.setQueueLimit(sender, mapName, limit, player, plugin);
    }

    private void executeJoin(CommandSender sender, String mapName) {
        if (!(sender instanceof Player player)) return;
        if (!playerHasPermission("infection.join", player)) return;

        joinLeaveCommand.joinMap(mapName, player, plugin);
    }

    private void executeLeave(CommandSender sender) {
        if (!(sender instanceof Player player)) return;
        if (!playerHasPermission("infection.leave", player)) {
            return;
        }

        joinLeaveCommand.leaveMap(player, plugin);
    }

    private void executeSetPos1(CommandSender sender) {
        Player player = verifyAdmin(sender);
        if (player == null) return;

        Location location = getFootBlockLocation(player);
        locationSelection.setPos1(location);
        MMUtils.sendMessage(player, "<light_purple>Position 1 set to: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }

    private void executeSetPos2(CommandSender sender) {
        Player player = verifyAdmin(sender);
        if (player == null) return;

        Location location = getFootBlockLocation(player);
        locationSelection.setPos2(location);
        MMUtils.sendMessage(player, "<light_purple>Position 2 set to: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }

    private void executeDiscardSelection(CommandSender sender) {
        Player player = verifyAdmin(sender);
        if (player == null) return ;

        locationSelection.setPos1(null);
        locationSelection.setPos2(null);
        MMUtils.sendMessage(player, PREFIX + "<light_purple>Location selection discarded.");
    }

    private void executeWand(CommandSender sender) {
        Player player = verifyAdmin(sender);
        if (player == null) return;

        ItemStack wand = new ItemStack(Material.STICK);
        wand.editMeta(meta -> {

            meta.displayName(mm.deserialize("<!italic><light_purple>Selection Wand"));
            meta.lore(List.of(
                    mm.deserialize("<!italic><gray>Selects a region with left and right clicks,"),
                    mm.deserialize("<!italic><gray>which can then be used to reate an Infection map."),
                    mm.deserialize(""),
                    mm.deserialize("<!italic><gray>Command aliases: <yellow>/infection pos1 <gray>& <yellow>/infection pos2"),
                    mm.deserialize(""),
                    mm.deserialize("<!italic><green>Left click to select point A."),
                    mm.deserialize("<!italic><green>Right click to select point B.")
            ));

            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            NamespacedKey key = new NamespacedKey(plugin, "infection_selection_wand");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        });

        PlayerInventory currentInventory = player.getInventory();
        ItemStack[] currentItems = player.getInventory().getContents().clone();
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(wand);
        if (!leftover.isEmpty()) {
            // Inventory is full, notify player with title and sound effect
            Title inventoryFull = Title.title(
                    mm.deserialize("<red>Inventory Full!"),
                    Component.empty()
            );
            player.showTitle(inventoryFull);
            MMUtils.sendMessage(player, PREFIX + "<red><bold>Inventory full!");
            MMUtils.sendMessage(player, PREFIX + "<red>Failed to add <light_purple>Selection Wand<red> to your inventory!");
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.9f);
            return;
        }

        for (int slot = 0; slot < 9; slot++) {
            ItemStack oldItem = currentItems[slot];
            ItemStack newItem = player.getInventory().getItem(slot);

            if ((oldItem == null || oldItem.getType().isAir())
                    && newItem != null
                    && !newItem.getType().isAir()) {

                currentInventory.setHeldItemSlot(slot);
                break;
            }
        }

        MMUtils.sendMessage(player, PREFIX + "<light_purple>Selection Wand<green> added to your inventory.");
    }

    /*
    * Utilities
    */
    private Player verifyAdmin(CommandSender sender) {
        if (sender instanceof Player player) {
            if (player.hasPermission("infection.admin")) {
                return player;
            }
        }
        return null;
    }

    private Location getFootBlockLocation(Player player) {
        Location loc = player.getLocation();
        int x = loc.getBlockX();
        int y = loc.getBlockY() - 1;
        int z = loc.getBlockZ();
        return new Location(loc.getWorld(), x, y, z);
    }

    // Checks if a player has the required permission or is an admin.
    // Sends an error message if not.
    private boolean playerHasPermission(String permission, Player player) {
        if (player.hasPermission(permission)) {
            return true;
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
            MMUtils.sendMessage(player, "<red>You don't have permission to use this command!");
            return false;
        }
    }
}
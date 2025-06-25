package me.kalbskinder.patientZero.commands;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.systems.Queue;
import me.kalbskinder.patientZero.systems.QueueManager;
import me.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class BaseCommand implements CommandExecutor {
    private PatientZero plugin;
    private static String prefix = Prefixes.getPrefix();

    // Get the plugin instance
    public BaseCommand(PatientZero plugin) {
        this.plugin = plugin;
    }


    // Send a message to the player when the executed command was not correct.
    public static void sendCorrectUsage(Player player, String usage) {
        player.sendMessage(prefix + "§cCorrect usage: " + usage);
    }


    /**
     * Executed when the base command '/ptz' is used.
     * Handles subcommands like help, createmap, deletemap, listmaps, join, and leave.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        // Command can only be executed by players
        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + "§cOnly players can execute this command.");
            return true;
        }

        // If no arguments were provided
        if (args.length == 0) {
            if (!playerHasPermission("ptz.admin", player)) {
                return true;
            }
            player.sendMessage(prefix + "§7 Use §e/ptz help §7 for a list of commands.");
            return true;
        }

        // Check which action is wanted
        switch (args[0]) {
            // Help command
            // Usage: /ptz help
            case "help" -> {
                if (!playerHasPermission("ptz.admin", player)) {
                    return true;
                }

                // TODO: Add hover effect that shows what the command is used for
                player.sendMessage(prefix + "§aAvailable commands:");
                player.sendMessage("§7- §e/ptz §6createmap §7<map-name> <x1> <y1> <z1> <x2> <y2> <z2>");
                player.sendMessage("§7- §e/ptz §6deletemap §7<map-name>");
                player.sendMessage("§7- §e/ptz §6addspawn §7<map-name> <role>");
                player.sendMessage("§7- §e/ptz §6setqueue-spawn §7<map-name>");
                player.sendMessage("§7- §e/ptz §6setqueue-limit §7<map-name> <int-limit>");
                player.sendMessage("§7- §e/ptz §6listmaps");
                player.sendMessage("§f");
                player.sendMessage("§7- §e/ptz §6join §7<map-name>");
                player.sendMessage("§7- §e/ptz §6leave");
            }

            // Create-map command
            // Usage: /ptz createmap <map-name> <x1> <y1> <z1> <x2> <y2> <z2>
            case "createmap" -> {
                if (!playerHasPermission("ptz.admin", player)) {
                    return true;
                }

                if (args.length != 8) {
                    sendCorrectUsage(player, "§e/ptz §6createmap §7<map-name> <x1> <y1> <z1> <x2> <y2> <z2>");
                    return true;
                }

                CreateMapCommand.createMap(sender, args, player, plugin);
            }

            // List-maps command
            // Usage: /ptz listmaps
            case "listmaps" -> {
                if (!playerHasPermission("ptz.admin", player)) {
                    return true;
                }

                if (args.length != 1) {
                    sendCorrectUsage(player, "§e/ptz listmaps");
                }

                ListCommands.listMaps(sender, args, player, plugin);
            }

            // Delete-map command
            // Usage: /ptz deletemap <map-name>
            case "deletemap" -> {
                if (!playerHasPermission("ptz.admin", player)) {
                    return true;
                }

                if (args.length != 2) {
                    sendCorrectUsage(player, "§e/ptz §6deletemap §7<map-name>");
                    return true;
                }

                DeleteMapCommand.deleteMap(sender, args, player, plugin);
            }

            // Add spawn command
            // Usage: '/ptz addspawn <map-name> <role>'
            // Adds a spawn location to the config.yml file for a specific role.
            // When the game starts the player with that role will be teleported to a random location from the config file
            case "addspawn" -> {
                if (!playerHasPermission("ptz.admin", player)) {
                    return true;
                }

                if (args.length != 3) {
                    sendCorrectUsage(player, "§e/ptz §6addspawn §7 <map-name> <role>");
                    player.sendMessage(prefix + "§6§lTIP! §rYou can set multiple spawn locations for a role.");
                    return true;
                }

                if (!args[2].equalsIgnoreCase("corrupted") && !args[2].equalsIgnoreCase("survivor")) {
                    player.sendMessage(prefix + "§c<role> has to be \"corrupted\" or \"survivor\".");
                    return true;
                }

                MapSpawnsCommand.addMapSpawns(sender, args, player, plugin);
            }

            // Set queue-spawn command
            // usage: '/ptz setqueue-spawn <map-name>'
            // Adds a spawn point to the config file where players will be teleported to if they join a queue
            // If no spawn point is given, teleports the player to a random map location
            case "setqueue-spawn" -> {
                if (!playerHasPermission("ptz.admin", player)) {
                    return true;
                }

                if (args.length != 2) {
                    sendCorrectUsage(player, "§e/ptz §setqueue-spawn §7<map-name>");
                    return true;
                }

                MapSpawnsCommand.setQueueSpawn(sender, args, player, plugin);
            }

            case "setqueue-limit" -> {
                if (!playerHasPermission("ptz.admin", player)) {
                    return true;
                }

                if (args.length != 3) {
                    sendCorrectUsage(player, "§e/ptz §6setqueue-limit §7<map-name> <int-limit>");
                    return true;
                }

                Queue.setQueueLimit(sender, args, player, plugin);
            }

            // Join command
            // Usage: /ptz join <map-name>
            case "join" -> {
                if (!playerHasPermission("ptz.join", player)) {
                    return true;
                }

                if (args.length != 2) {
                    sendCorrectUsage(player, "§e/ptz §6join §7<map-name>");
                    return true;
                }

                JoinLeaveCommand.joinMap(args, player, plugin);
            }

            // Leave command
            // Usage: /ptz leave
            case "leave" -> {
                if (!playerHasPermission("ptz.leave", player)) {
                    return true;
                }

                if (args.length != 1) {
                    sendCorrectUsage(player, "§e/ptz leave");
                }

                JoinLeaveCommand.leaveMap(player, plugin);
            }

            case "reset" -> {
                QueueManager.getQueue("test");
            }

            // Default response if no match was found
            default -> {
                player.sendMessage(prefix + "§cUnknown subcommand. Use §e/ptz help §cfor a list of commands.");
            }
        }

        return true;
    }

    /**
     * Checks if a player has the required permission or is an admin.
     * Sends an error message if not.
     *
     * @param permission The required permission node.
     * @param player The player to check for
     * @return true if the player has permission, false otherwise.
     */
    private boolean playerHasPermission(String permission, Player player) {
        if (player.hasPermission(permission)) {
            return true;
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
            player.sendMessage(prefix + "§cYou don't have permission to use this command!");
            return false;
        }
    }
}
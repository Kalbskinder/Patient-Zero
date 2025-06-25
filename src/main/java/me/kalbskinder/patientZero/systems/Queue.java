package me.kalbskinder.patientZero.systems;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.enums.GameState;
import me.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class Queue {
    private static String prefix = Prefixes.getPrefix();

    // Sets the queue limit in the config file
    // If the limit on players is reached, no more can join
    public static void setQueueLimit(CommandSender sender, String[] args, Player player, PatientZero plugin) {
        String mapName = args[1];
        FileConfiguration config = plugin.getConfig();
        int maxLimit = Integer.parseInt(args[2]);

        if (!config.contains("maps." + mapName)) {
            sender.sendMessage(prefix + "§cMap not found!");
            return;
        }

        config.set("maps." + mapName + ".queue-limit", maxLimit);
        plugin.saveConfig();
        sender.sendMessage(prefix + "§aSet queue limit to §6" + args[2] + "§a for map §e" + mapName);
    }

    // Manages player requests to join a queue
    public static boolean canPlayerJoinQueue(Player player, String mapName, PatientZero plugin) {
        String customPrefix = Prefixes.getCustomPrefix();
        FileConfiguration config = plugin.getConfig();

        if (!config.contains("maps." + mapName)) {
            player.sendMessage(prefix + "§cMap does not exist!");
            return false;
        }

        int maxQueueSize = config.getInt("maps." + mapName + ".queue-limit", 8);

        List<Player> currentQueue = QueueManager.getQueue(mapName);
        GameState gameState = QueueManager.getGameState(mapName);

        if (currentQueue.contains(player)) {
            player.sendMessage(customPrefix + "§eYou are already in the queue for §6" + mapName);
            return false;
        }

        if (currentQueue.size() >= maxQueueSize) {
            player.sendMessage(customPrefix + "§cQueue is full!");
            return false;
        }

        if (gameState == GameState.INGAME || gameState == GameState.STARTING) {
            player.sendMessage(customPrefix + "§cThe game is currently in progress!");
            return false;
        }

        if (gameState == GameState.ENDING) {
            player.sendMessage(customPrefix + "§eThe game is ending. Try again in a view seconds!");
            return false;
        }

        return true;
    }
}


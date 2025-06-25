package me.kalbskinder.patientZero.commands;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.enums.GameState;
import me.kalbskinder.patientZero.systems.Queue;
import me.kalbskinder.patientZero.systems.QueueManager;
import me.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Logger;

public class JoinLeaveCommand {
    private static String prefix = Prefixes.getPrefix();


    public static void joinMap(String[] args, Player player, PatientZero plugin) {
        FileConfiguration config = plugin.getConfig();

        String customPrefix = Prefixes.getCustomPrefix();
        String joinMessage = config.getString("messages.playerjoin", "You joined the queue");

        String mapName = args[1];

        // Read the saved map spawn
        List<?> data = config.getList("maps." + mapName + ".spawns.queue-spawn");
        if (data != null && data.size() == 6) {
            World world = Bukkit.getWorld((String) data.get(0));
            double x = (double) data.get(1);
            double y = (double) data.get(2);
            double z = (double) data.get(3);
            float yaw = ((Double) data.get(4)).floatValue(); // Can't directly cast float to double
            float pitch = ((Double) data.get(5)).floatValue();

            if (world != null) {
                Location loc = new Location(world, x, y, z, yaw, pitch); // Location to teleport to

                // Only teleport the player if he was able to join the queue
                if (Queue.canPlayerJoinQueue(player, mapName, plugin)) { // Check if the player is able to join the queue
                    player.teleport(loc); // Teleport the player to the defined location
                    QueueManager.addToQueue(mapName, player); // Add the player to the queue
                    player.sendMessage(customPrefix + updateJoinLeaveMessages(joinMessage, customPrefix, player, plugin, false));
                }
            } else {
                player.sendMessage(prefix + "§cWorld '" + data.get(0) + "' was not found.");
            }
        } else {
            player.sendMessage(prefix + "§cMap not found or no spawn point registered. Use '/ptz setqueue-spawn <map-name>'");
        }
    }

    public static void leaveMap(Player player, PatientZero plugin) {
        FileConfiguration config = plugin.getConfig();

        if (!QueueManager.isPlayerQueued(player)) {
            return;
        }

        GameState gameState = QueueManager.getGameState(QueueManager.getMapOfPlayer(player));

        String executeCommand = config.getString("settings.executes.playerOnLeaveQueue", "/me Teleport me!");
        executeCommand = executeCommand.substring(1);

        if (gameState != GameState.INGAME && gameState != GameState.ENDING && gameState != GameState.STARTING) {
            String customPrefix = Prefixes.getCustomPrefix();
            String leaveMessage = config.getString("messages.playerleave", "You left the queue");

            String updatedLeaveMessage = updateJoinLeaveMessages(leaveMessage, customPrefix, player, plugin, true);

            if(QueueManager.removePlayerFromAnyQueue(player)) {
                player.sendMessage(customPrefix + updatedLeaveMessage);
                player.performCommand(executeCommand);
            } else {
                player.sendMessage(customPrefix + "§cUnable to leave queue.");
            }
        } else {
            QueueManager.removePlayerFromAnyQueue(player);
            player.performCommand(executeCommand);
        }
    }

    private static String updateJoinLeaveMessages(String message, String customPrefix, Player player, PatientZero plugin, Boolean leave) {
        FileConfiguration config = plugin.getConfig();

        String mapName = QueueManager.getMapOfPlayer(player);
        String playerName = player.getName();

        List<Player> queue = QueueManager.getQueue(mapName);

        int maxQueueSize = config.getInt("maps." + mapName + ".queue-limit", 8);
        int queueSize = queue.size();

        // Read the broadcast join/leave message from the config file
        String playerLeaveBroadcast = config.getString("messages.broadcast-playerleave", "A player left the queue");
        String playerJoinBroadcast = config.getString("messages.broadcast-playerjoin", "A player joined the queue");

        // Replace placeholders in player leave message with values
        playerLeaveBroadcast = playerLeaveBroadcast.replace("%player%", playerName);
        playerLeaveBroadcast = playerLeaveBroadcast.replace("%player-count%", String.valueOf(queue.size() - 1));
        playerLeaveBroadcast = playerLeaveBroadcast.replace("%max-player-count%", String.valueOf(maxQueueSize));
        final String broadcastPlayerLeaveMessage = playerLeaveBroadcast; // Needs to be finals because it's used in lambda

        // Replace placeholders in player join message with values
        playerJoinBroadcast = playerJoinBroadcast.replace("%player%", playerName);
        playerJoinBroadcast = playerJoinBroadcast.replace("%player-count%", String.valueOf(queue.size()));
        playerJoinBroadcast = playerJoinBroadcast.replace("%max-player-count%", String.valueOf(maxQueueSize));
        final String broadcastPlayerJoinMessage = playerJoinBroadcast; // Needs to be final because it's ues in lambda


        // Broadcast the join and leave message to all players who are in teh same queue
        if (leave) {
            queueSize -= 1; // Remove the player when leaving the queue
            queue.forEach(p -> {
                // Don't display the message for the player who left
                if (!p.getName().equals(playerName)) {
                    p.sendMessage(customPrefix + broadcastPlayerLeaveMessage);
                }
            });
        } else {
            queue.forEach(p -> {
                // Don't display the message for the player who left
                if(!p.getName().equals(playerName)) {
                    p.sendMessage(customPrefix + broadcastPlayerJoinMessage);
                }
            });
        }

        // Replace placeholders in the private join/leave message (this message won't be shown to other players)
        message = message.replace("%player%", playerName);
        message = message.replace("%player-count%", String.valueOf(queueSize));
        message = message.replace("%max-player-count%", String.valueOf(maxQueueSize));

        return message;
    }
}

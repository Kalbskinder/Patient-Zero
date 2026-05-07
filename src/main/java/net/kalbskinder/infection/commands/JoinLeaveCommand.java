package net.kalbskinder.infection.commands;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.infection.Infection;
import net.kalbskinder.infection.enums.GameState;
import net.kalbskinder.infection.systems.Queue;
import net.kalbskinder.infection.systems.QueueManager;
import net.kalbskinder.infection.utils.MMUtils;
import net.kalbskinder.infection.utils.Prefixes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

@RequiredArgsConstructor
public class JoinLeaveCommand {
    private final Queue queue;
    private final QueueManager queueManager;

    private static final String PREFIX = Prefixes.getPrefix();

    public void joinMap(String mapName, Player player, Infection plugin) {
        FileConfiguration config = plugin.getConfig();

        String customPrefix = Prefixes.getCustomPrefix();
        String joinMessage = config.getString("messages.playerjoin", "You joined the queue");

        // Read the saved map spawn
        List<?> data = config.getList("maps." + mapName + ".spawns.queue-spawn");
        if (data != null && data.size() == 6) {
            if (!(data.get(0) instanceof String worldName)
                    || !(data.get(1) instanceof Number xRaw)
                    || !(data.get(2) instanceof Number yRaw)
                    || !(data.get(3) instanceof Number zRaw)
                    || !(data.get(4) instanceof Number yawRaw)
                    || !(data.get(5) instanceof Number pitchRaw)) {
                MMUtils.sendMessage(player, PREFIX + "<red>Queue spawn data for <yellow>" + mapName + "<red> is invalid. Re-set it with '/infection setqueue-spawn " + mapName + "'.");
                return;
            }

            World world = Bukkit.getWorld(worldName);
            double x = xRaw.doubleValue();
            double y = yRaw.doubleValue();
            double z = zRaw.doubleValue();
            float yaw = yawRaw.floatValue();
            float pitch = pitchRaw.floatValue();

            if (world != null) {
                Location loc = new Location(world, x, y, z, yaw, pitch); // Location to teleport to

                // Only teleport the player if he was able to join the queue
                if (queue.canPlayerJoinQueue(player, mapName, plugin)) { // Check if the player is able to join the queue
                    player.teleport(loc); // Teleport the player to the defined location
                    queueManager.addToQueue(mapName, player); // Add the player to the queue
                    MMUtils.sendMessage(player, customPrefix + updateJoinLeaveMessages(joinMessage, customPrefix, player, plugin, false));
                }
            } else {
                MMUtils.sendMessage(player, PREFIX + "<red>World '" + data.getFirst() + "' was not found.");
            }
        } else {
            MMUtils.sendMessage(player, PREFIX + "<red>Map not found or no spawn point registered. Use '/infection setqueue-spawn <map-name>'");
        }
    }

    public void leaveMap(Player player, Infection plugin) {
        FileConfiguration config = plugin.getConfig();

        if (!queueManager.isPlayerQueued(player)) {
            return;
        }

        GameState gameState = queueManager.getGameState(queueManager.getMapOfPlayer(player));

        String executeCommand = config.getString("settings.executes.playerOnLeaveQueue", "/me Teleport me!");
        executeCommand = executeCommand.substring(1);

        if (gameState != GameState.INGAME && gameState != GameState.ENDING && gameState != GameState.STARTING) {
            String customPrefix = Prefixes.getCustomPrefix();
            String leaveMessage = config.getString("messages.playerleave", "You left the queue");

            // Replace placeholders in the leave message
            String updatedLeaveMessage = updateJoinLeaveMessages(leaveMessage, customPrefix, player, plugin, true);

            if(queueManager.removePlayerFromAnyQueue(player)) {
                MMUtils.sendMessage(player, customPrefix + updatedLeaveMessage);
                player.performCommand(executeCommand);
            } else {
                MMUtils.sendMessage(player, customPrefix + "<red>Unable to leave queue.");
            }
        } else {
            // Remove player from queue without notifying others
            queueManager.removePlayerFromAnyQueue(player);
            player.performCommand(executeCommand);
        }
    }

    private String updateJoinLeaveMessages(String message, String customPrefix, Player player, Infection plugin, Boolean leave) {
        FileConfiguration config = plugin.getConfig();

        String mapName = queueManager.getMapOfPlayer(player);
        String playerName = player.getName();

        List<Player> queue = queueManager.getQueue(mapName);

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
                    MMUtils.sendMessage(p, customPrefix + broadcastPlayerLeaveMessage);
                }
            });
        } else {
            queue.forEach(p -> {
                // Don't display the message for the player who left
                if(!p.getName().equals(playerName)) {
                    MMUtils.sendMessage(p, customPrefix + broadcastPlayerJoinMessage);
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

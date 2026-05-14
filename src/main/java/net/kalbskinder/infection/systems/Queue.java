package net.kalbskinder.infection.systems;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.infection.Infection;
import net.kalbskinder.infection.enums.GameState;
import net.kalbskinder.infection.utils.MMUtils;
import net.kalbskinder.infection.utils.Prefixes;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

@RequiredArgsConstructor
public class Queue {
    private final QueueManager queueManager;
    private static final String PREFIX = Prefixes.getPrefix();

    // Sets the queue limit in the config file
    // If the limit on players is reached, no more can join
    public void setQueueLimit(CommandSender sender, String mapName, int maxLimit, Player player, Infection plugin) {
        FileConfiguration config = plugin.getConfig();

        if (!config.contains("maps." + mapName)) {
            MMUtils.sendMessage(player, PREFIX + "<red>Map not found!");
            return;
        }

        config.set("maps." + mapName + ".queue-limit", maxLimit);
        plugin.saveConfig();
        MMUtils.sendMessage(player, PREFIX + "<green>Set queue limit to <gold>" + maxLimit + "<green> for map <yellow>" + mapName);
    }

    // Manages player requests to join a queue
    public boolean canPlayerJoinQueue(Player player, String mapName, Infection plugin) {
        String customPrefix = Prefixes.getCustomPrefix();
        FileConfiguration config = plugin.getConfig();

        if (!config.contains("maps." + mapName)) {
            MMUtils.sendMessage(player, PREFIX + "<red>Map does not exist!");
            return false;
        }

        int maxQueueSize = config.getInt("maps." + mapName + ".queue-limit", 8);

            List<Player> currentQueue = queueManager.getQueue(mapName);
        GameState gameState = queueManager.getGameState(mapName);

        if (currentQueue.contains(player)) {
            MMUtils.sendMessage(player, customPrefix + "<red>You are already in the queue for <gold>" + mapName);
            return false;
        }

        if (currentQueue.size() >= maxQueueSize) {
            MMUtils.sendMessage(player, customPrefix + "<red>Queue is full!");
            return false;
        }

        if (gameState == GameState.INGAME || gameState == GameState.STARTING) {
            MMUtils.sendMessage(player, customPrefix + "<red>The game is currently in progress!");
            return false;
        }

        if (gameState == GameState.ENDING) {
            MMUtils.sendMessage(player, customPrefix + "<red>The game is ending. Try again in a view seconds!");
            return false;
        }

        return true;
    }
}


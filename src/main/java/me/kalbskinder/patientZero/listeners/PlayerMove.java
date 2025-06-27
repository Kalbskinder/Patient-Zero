package me.kalbskinder.patientZero.listeners;

import me.kalbskinder.patientZero.enums.GameState;
import me.kalbskinder.patientZero.systems.QueueManager;
import me.kalbskinder.patientZero.systems.TeleportPlayers;
import me.kalbskinder.patientZero.utils.MMUtils;
import me.kalbskinder.patientZero.utils.PlayerCheck;
import me.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;

public class PlayerMove implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        // Check if the player really moved (ignore small movements)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        if (!QueueManager.isPlayerQueued(player)) return; // Exit when player is not queued

        if (QueueManager.getGameState(QueueManager.getMapOfPlayer(player)) != GameState.INGAME) {
            player.setAllowFlight(false);
        }

        // Saturate the player
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setExhaustion(0f);

        String mapName = QueueManager.getMapOfPlayer(player);
        GameState gameState = QueueManager.getGameState(mapName);

        // Only check if the game is ongoing
        if (gameState != GameState.INGAME && gameState != GameState.ENDING && gameState != GameState.STARTING) return;

        // Get the map corners from the config file
        ArrayList<Location> mapCorners = PlayerCheck.getMapArea(mapName, player.getWorld());

        // Check if the player has left the map area
        if (!PlayerCheck.isInsideArea(player.getLocation(), mapCorners.get(0), mapCorners.get(1))) {

            // Don't remove respawning players form queue
            if (player.getGameMode() == GameMode.SPECTATOR) {
                TeleportPlayers.teleportPlayerToCorruptedLocations(player);
                MMUtils.sendMessage(player, Prefixes.getCustomPrefix() + "<red>You can't leave this area");
                return;
            }

            QueueManager.removePlayerFromAnyQueue(player);

            // Only send the message if the action happened in game not while ending
            if (gameState == GameState.INGAME) {
                MMUtils.sendMessage(player, Prefixes.getCustomPrefix() + "<red>You left the map area and were kicked from the game!");
            }
        }

    }
}
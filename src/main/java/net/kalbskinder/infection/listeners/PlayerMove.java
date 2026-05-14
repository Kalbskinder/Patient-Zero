package net.kalbskinder.infection.listeners;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.infection.enums.GameState;
import net.kalbskinder.infection.systems.QueueManager;
import net.kalbskinder.infection.systems.TeleportPlayers;
import net.kalbskinder.infection.utils.MMUtils;
import net.kalbskinder.infection.utils.PlayerCheck;
import net.kalbskinder.infection.utils.Prefixes;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;

@RequiredArgsConstructor
public class PlayerMove implements Listener {
    private final QueueManager queueManager;
    private final PlayerCheck playerCheck;
    private final TeleportPlayers teleportPlayers;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        // Check if the player really moved (ignore small movements)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        if (!queueManager.isPlayerQueued(player)) return; // Exit when player is not queued

        if (queueManager.getGameState(queueManager.getMapOfPlayer(player)) != GameState.INGAME) {
            player.setAllowFlight(false);
        }

        // Saturate the player
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setExhaustion(0f);

        String mapName = queueManager.getMapOfPlayer(player);
        GameState gameState = queueManager.getGameState(mapName);

        // Only check if the game is ongoing
        if (gameState != GameState.INGAME && gameState != GameState.ENDING && gameState != GameState.STARTING) return;

        // Get the map corners from the config file
        ArrayList<Location> mapCorners = playerCheck.getMapArea(mapName, player.getWorld());

        // Check if the player has left the map area
        if (!playerCheck.isInsideArea(player.getLocation(), mapCorners.get(0), mapCorners.get(1))) {

            // Don't remove respawning players form queue
            if (player.getGameMode() == GameMode.SPECTATOR) {
                teleportPlayers.teleportPlayerToInfectedLocations(player);
                MMUtils.sendMessage(player, Prefixes.getCustomPrefix() + "<red>You can't leave this area");
                return;
            }

            queueManager.removePlayerFromAnyQueue(player);

            // Only send the message if the action happened in game not while ending
            if (gameState == GameState.INGAME) {
                MMUtils.sendMessage(player, Prefixes.getCustomPrefix() + "<red>You left the map area and were kicked from the game!");
            }
        }

    }
}
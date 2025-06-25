package me.kalbskinder.patientZero.listeners;

import me.kalbskinder.patientZero.systems.QueueManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!QueueManager.isPlayerQueued(player)) return; // Return if player wasn't queued

        // Remove player from queue
        QueueManager.removePlayerFromAnyQueue(player);
        player.getInventory().clear();
        player.setAllowFlight(false);
    }
}

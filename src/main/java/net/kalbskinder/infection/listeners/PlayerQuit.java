package net.kalbskinder.infection.listeners;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.infection.systems.QueueManager;
import net.kalbskinder.infection.systems.LocationSelection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class PlayerQuit implements Listener {
    private final QueueManager queueManager;
    private final LocationSelection locationSelection;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        locationSelection.clearSelection(player);
        if (!queueManager.isPlayerQueued(player)) return; // Return if player wasn't queued

        // Remove player from queue
        queueManager.removePlayerFromAnyQueue(player);
        player.getInventory().clear();
        player.setAllowFlight(false);
    }
}

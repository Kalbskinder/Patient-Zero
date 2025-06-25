package me.kalbskinder.patientZero.listeners;

import me.kalbskinder.patientZero.systems.QueueManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangeWorld implements Listener {
    @EventHandler
    public void onPlayerLeaveWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (QueueManager.isPlayerQueued(player)) {
            QueueManager.removePlayerFromAnyQueue(player); // Remove from queue
            player.setAllowFlight(false);
        }
    }
}

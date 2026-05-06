package net.kalbskinder.patientZero.listeners;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.patientZero.systems.QueueManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

@RequiredArgsConstructor
public class PlayerChangeWorld implements Listener {
    private final QueueManager queueManager;

    @EventHandler
    public void onPlayerLeaveWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (queueManager.isPlayerQueued(player)) {
            queueManager.removePlayerFromAnyQueue(player); // Remove from queue
            player.setAllowFlight(false);
        }
    }
}

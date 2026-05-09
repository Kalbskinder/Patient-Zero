package net.kalbskinder.infection.listeners;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.infection.systems.LocationSelection;
import net.kalbskinder.infection.systems.QueueManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

@RequiredArgsConstructor
public class PlayerChangeWorld implements Listener {
    private final QueueManager queueManager;
    private final LocationSelection locationSelection;

    @EventHandler
    public void onPlayerLeaveWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        locationSelection.clearSelection(player);

        if (queueManager.isPlayerQueued(player)) {
            queueManager.removePlayerFromAnyQueue(player); // Remove from queue
            player.setAllowFlight(false);
        }
    }
}

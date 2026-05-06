package net.kalbskinder.patientZero.listeners;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.patientZero.systems.QueueManager;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

@RequiredArgsConstructor
public class EntityShootArrow implements Listener {
    private final QueueManager queueManager;

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Arrow arrow) {
            if (arrow.getShooter() instanceof Player p) {
                if (!queueManager.isPlayerQueued(p)) return; // Check if the player was queued
                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED); // Arrows can't be picked up
            }
        }
    }
}

package me.kalbskinder.patientZero.listeners;

import me.kalbskinder.patientZero.systems.QueueManager;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class EntityShootArrow implements Listener {
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Arrow arrow) {
            if (arrow.getShooter() instanceof Player p) {
                if (!QueueManager.isPlayerQueued(p)) return; // Check if the player was queued
                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED); // Arrows can't be picked up
            }
        }
    }
}

package me.kalbskinder.patientZero.listeners;

import me.kalbskinder.patientZero.systems.QueueManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class PlayerBreakBlock implements Listener {
    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        if (QueueManager.isPlayerQueued(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}

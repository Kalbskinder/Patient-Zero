package net.kalbskinder.infection.listeners;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.infection.systems.QueueManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@RequiredArgsConstructor
public class PlayerBreakBlock implements Listener {
    private final QueueManager queueManager;

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        if (queueManager.isPlayerQueued(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}

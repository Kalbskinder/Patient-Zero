package net.kalbskinder.infection.listeners;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.infection.enums.PlayerRole;
import net.kalbskinder.infection.systems.QueueInfo;
import net.kalbskinder.infection.systems.QueueManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class PlayerDropItem implements Listener {
    private final QueueManager queueManager;

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Detect if the player is queued
        if (queueManager.isPlayerQueued(player)) {
            event.setCancelled(true); // Cancel the event (item will not be dropped)
        }

    }

    // Check if a player is trying to remove the armor he's currently wearing
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!queueManager.isPlayerQueued(player)) return; // Check if player is queued

        String map = queueManager.getMapOfPlayer(player);
        QueueInfo queue = queueManager.getQueueInfo(map);
        PlayerRole role = queue.getRoles().get(player);

        // Only infected players have armor, check if he tried taking off his armor
        if (role == PlayerRole.INFECTED) {
            InventoryType.SlotType slotType = event.getSlotType();

            // Block armor removing
            if (slotType == InventoryType.SlotType.ARMOR || event.getClick().isShiftClick()) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem != null && currentItem.getType() != Material.AIR) {
                    event.setCancelled(true);
                }
            }
        }
    }
}

package me.kalbskinder.patientZero.listeners;

import me.kalbskinder.patientZero.enums.PlayerRole;
import me.kalbskinder.patientZero.systems.QueueInfo;
import me.kalbskinder.patientZero.systems.QueueManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDropItem implements Listener {

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Detect if the player is queued
        if (QueueManager.isPlayerQueued(player)) {
            event.setCancelled(true); // Cancel the event (item will not be dropped)
        }

    }

    // Check if a player is trying to remove the armor he's currently wearing
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!QueueManager.isPlayerQueued(player)) return; // Check if player is queued

        String map = QueueManager.getMapOfPlayer(player);
        QueueInfo queue = QueueManager.getQueueInfo(map);
        PlayerRole role = queue.getRoles().get(player);

        // Only corrupted players have armor, check if he tried taking off his armor
        if (role == PlayerRole.CORRUPTED) {
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

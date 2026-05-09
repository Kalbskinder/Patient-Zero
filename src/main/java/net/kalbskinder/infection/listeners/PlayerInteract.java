package net.kalbskinder.infection.listeners;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.infection.Infection;
import net.kalbskinder.infection.systems.LocationSelection;
import net.kalbskinder.infection.utils.MMUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@RequiredArgsConstructor
public class PlayerInteract implements Listener {
    private final Infection plugin;
    private final LocationSelection locationSelection;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        NamespacedKey requiredKey = new NamespacedKey(plugin, "infection_selection_wand");
        ItemStack item = event.getItem();
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(requiredKey)) {
            event.setCancelled(true);
            Action action = event.getAction();
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock == null) return;
            if (action == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                locationSelection.setPos2(event.getClickedBlock().getLocation());
                MMUtils.sendMessage(player, "<light_purple>Position 2 set to: " + clickedBlock.getX() + ", " + clickedBlock.getY() + ", " + clickedBlock.getZ());

            } else if (action == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
                locationSelection.setPos1(event.getClickedBlock().getLocation());
                MMUtils.sendMessage(player, "<light_purple>Position 1 set to: " + clickedBlock.getX() + ", " + clickedBlock.getY() + ", " + clickedBlock.getZ());

            }

        }
    }
}

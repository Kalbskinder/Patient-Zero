package net.kalbskinder.patientZero.utils;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class ItemActionHandler implements Listener {
    private final ItemMaker itemMaker;
    private final Map<String, Consumer<Player>> actions = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger("PTZ");

    /**
     * Registers a right-click action for an item.
     *
     * @param actionId The unique identifier for the action.
     * @param action   The action to perform when the item is right-clicked.
     */
    public void registerAction(String actionId, Consumer<Player> action) {
        if (actionId == null || actionId.trim().isEmpty()) {
            LOGGER.warning("Invalid actionId for item action registration");
            return;
        }
        actions.put(actionId, action);
    }

    // Executes when a player right-clicks something
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Ignore right-clicks on air and blocks
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Get the item that triggered the event
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        String actionId = itemMaker.getActionId(item);
        if (actionId == null) {
            return;
        }

        Consumer<Player> action = actions.get(actionId);
        if (action == null) {
            LOGGER.warning("No action registered for actionId: " + actionId);
            return;
        }

        Player player = event.getPlayer();
        action.accept(player);

        event.setCancelled(true);
    }
}
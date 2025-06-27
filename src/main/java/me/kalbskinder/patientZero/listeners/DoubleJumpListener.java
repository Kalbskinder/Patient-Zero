package me.kalbskinder.patientZero.listeners;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.enums.GameState;
import me.kalbskinder.patientZero.systems.QueueManager;
import me.kalbskinder.patientZero.utils.MMUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;
import org.bukkit.entity.*;

import java.util.HashMap;
import java.util.UUID;

public class DoubleJumpListener implements Listener {
    private final boolean isDoubleJumpEnabled;
    private final String cooldownMessage;
    private final double velocity;
    private final double cooldownSeconds;

    private final HashMap<UUID, Long> lastJumpTime = new HashMap<>();

    // Read settings from the config.yml on plugin startup
    public DoubleJumpListener(PatientZero plugin) {
        FileConfiguration config = plugin.getConfig();
        this.isDoubleJumpEnabled = config.getBoolean("settings.double-jump.enabled", false);
        this.velocity = config.getDouble("settings.double-jump.velocity", 0.5);
        this.cooldownSeconds = config.getDouble("settings.double-jump.cooldown", 5);
        this.cooldownMessage = config.getString("settings.double-jump.cooldown-message", "<red>Please wait %time%s before jumping again.");
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE) {
            player.setAllowFlight(true);
            event.setCancelled(false);
            return;
        }

        // Cancel default flight and handle jump
        event.setCancelled(true);

        // Abort early if not applicable
        if (!isDoubleJumpEnabled || player.getGameMode() == GameMode.CREATIVE || !QueueManager.isPlayerQueued(player)) {
            return;
        }

        // Only allow double jumping in game
        GameState gameState = QueueManager.getGameState(QueueManager.getMapOfPlayer(player));
        if (gameState == GameState.WAITING || gameState == GameState.COUNTDOWN) {
            return;
        }

        player.setAllowFlight(false);

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = lastJumpTime.getOrDefault(uuid, 0L);
        long elapsed = now - last;

        // Check cooldown
        if (elapsed < (cooldownSeconds * 1000)) {
            long remaining = (long) Math.ceil((cooldownSeconds * 1000 - elapsed) / 1000.0);
            MMUtils.sendMessage(player, cooldownMessage.replace("%time%", String.valueOf(remaining)));
            return;
        }

        // Launch the player forward and upward
        Vector jump = player.getLocation().getDirection().multiply(0.6).setY(velocity);
        player.setVelocity(jump);

        // Play effects
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 0.5, 0), 20, 0.3, 0.2, 0.3, 0.01);
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1f, 1f);

        // Save cooldown timestamp
        lastJumpTime.put(uuid, now);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE) {
            player.setAllowFlight(true);
        }

        // Only if the player is in a queue
        if (!QueueManager.isPlayerQueued(player)) return;

        if (((Entity) player).isOnGround()) {
                player.setAllowFlight(true);
        }
    }
}

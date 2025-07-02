package me.kalbskinder.patientZero.listeners;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.enums.GameState;
import me.kalbskinder.patientZero.enums.PlayerRole;
import me.kalbskinder.patientZero.systems.ItemDistributor;
import me.kalbskinder.patientZero.systems.QueueInfo;
import me.kalbskinder.patientZero.systems.QueueManager;
import me.kalbskinder.patientZero.systems.TeleportPlayers;
import me.kalbskinder.patientZero.systems.scoreboard.GameSessionStats;
import me.kalbskinder.patientZero.systems.scoreboard.ScoreboardSessionManager;
import me.kalbskinder.patientZero.utils.MMUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

public class PlayerTakeDamage implements Listener {
    public static FileConfiguration config;
    private static PatientZero plugin;

    public PlayerTakeDamage(PatientZero main) {
        config = main.getConfig();
        plugin = main;
    }


    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        // Check if hurt entity is a player
        if (e.getEntity() instanceof Player)  {
            Player victim = (Player) e.getEntity(); // The player that got hurt
            Entity damager = e.getDamager(); // The Entity that hurt the player

            // Check if the player was queued
            if (!QueueManager.isPlayerQueued(victim)) return;

            String mapName = QueueManager.getMapOfPlayer(victim);
            QueueInfo queue = QueueManager.getQueueInfo(mapName);

            // Only allow damage in game
            if (queue.getState() != GameState.INGAME) {
                e.setCancelled(true);
                return;
            }

            // Saturate the player
            victim.setFoodLevel(20);
            victim.setSaturation(20f);
            victim.setExhaustion(0f);

            // If the player was shot with an arrow
            if (damager instanceof Arrow arrow) {
                // Check if he was shot by another player
                if (arrow.getShooter() instanceof Player shooter) {
                    Map<Player, PlayerRole> playerRoles = queue.getRoles();
                    PlayerRole victimRole = playerRoles.get(victim);
                    PlayerRole shooterRole = playerRoles.get(shooter);

                    GameSessionStats stats = ScoreboardSessionManager.getSession(mapName);

                    // Survivors can't hurt each other
                    if (victimRole == PlayerRole.SURVIVOR && shooterRole == PlayerRole.SURVIVOR) {
                        e.setCancelled(true);
                        return;
                    }

                    if (victimRole == PlayerRole.CORRUPTED && shooterRole == PlayerRole.SURVIVOR) {
                        // Update the kill counter
                        int currentKills = stats.getPlayerKills().getOrDefault(shooter.getUniqueId(), 0);
                        stats.getPlayerKills().put(shooter.getUniqueId(), currentKills + 1);

                        if (isRoleAlive(mapName, PlayerRole.PATIENT_ZERO)) {
                            corruptedRespawn(victim); // Start respawn cycle
                            return;
                        }

                        stats.setCorruptedCount(stats.getCorruptedCount() - 1);
                        playerRoles.remove(victim); // Player can no longer respawn

                        // Check if there's any other corrupted players left
                        if (!isRoleAlive(mapName, PlayerRole.CORRUPTED)) {
                            queue.setGameWinners(PlayerRole.SURVIVOR); // Set game winners as survivors
                            QueueManager.gameEnd(queue, mapName);
                            return;
                        }

                        // Display player a title
                        String title = config.getString("titles.roles.final-death.title", "<red>You died!");
                        String subtitle = config.getString("titles.roles.final-death.subtitle", "<yellow>You can't respawn anymore!");
                        MMUtils.displayTitle(victim, title, subtitle, 1f, 3f, 1f);
                        victim.performCommand(config.getString("settings.executes.playerOnFinalDeath", "/me Teleport me!").substring(1));
                    }

                    // Only survivors can damage the patient-zero
                    if (victimRole == PlayerRole.PATIENT_ZERO && shooterRole == PlayerRole.SURVIVOR) {
                        // Update the kill counter
                        int currentKills = stats.getPlayerKills().getOrDefault(shooter.getUniqueId(), 0);
                        stats.getPlayerKills().put(shooter.getUniqueId(), currentKills + 1);

                        // Check if there's any corrupted alive, else survivors win
                        if (!isRoleAlive(mapName, PlayerRole.CORRUPTED)) {
                            queue.setGameWinners(PlayerRole.SURVIVOR); // Set game winners as survivors
                            QueueManager.gameEnd(queue, mapName); // End the game
                            return;
                        }

                        String title = config.getString("titles.roles.final-death.title", "<red>You died!");
                        MMUtils.displayTitle(victim, title, "", 1f, 3f, 1f);
                        victim.performCommand(config.getString("settings.executes.playerOnFinalDeath", "/me Teleport me!").substring(1));

                        // Read message from the config and notify players
                        queue.getPlayers().forEach(player -> {
                            MMUtils.sendMessage(player, config.getString("messages.ptz-dead", "<red><bold>Patient-Zero died! <reset><red>It was %player%").replace("%player%", victim.getName()));
                            MMUtils.sendMessage(player, config.getString("message.ptz-dead-info", "<yellow>Corrupted players will no longer respawn!").replace("%player%", victim.getName()));
                            player.playSound(player.getLocation(), Sound.ENTITY_BAT_DEATH, 0.8f, 0.8f);
                        });

                        // Subtract the corrupted player count by one
                        stats.setCorruptedCount(stats.getCorruptedCount() - 1);

                        victim.setGameMode(GameMode.SPECTATOR);
                        playerRoles.remove(victim); // Remove player from list
                    }

                } else {
                    e.setCancelled(true);
                    return;
                }
            }

            // If the player was damaged by another player
            if (damager instanceof Player) {

                // Get player roles
                Map<Player, PlayerRole> playerRoles = queue.getRoles();
                PlayerRole victimRole = playerRoles.get(victim);
                PlayerRole damagerRole = playerRoles.get(damager);

                // Patient-Zero can't get damaged by another player directly
                if (victimRole == PlayerRole.PATIENT_ZERO) {
                    e.setCancelled(true);
                    return;
                }

                if (victimRole == PlayerRole.CORRUPTED && damagerRole == PlayerRole.CORRUPTED) {
                    e.setCancelled(true);
                    return;
                }

                if (victimRole == PlayerRole.CORRUPTED && damagerRole == PlayerRole.PATIENT_ZERO) {
                    e.setCancelled(true);
                    return;
                }

                // Survivors can only damage player with their bows
                if (victimRole == PlayerRole.CORRUPTED && damagerRole == PlayerRole.SURVIVOR) {
                    e.setCancelled(true);
                    return;
                }

                // Survivors can't damage each others
                if (victimRole == PlayerRole.SURVIVOR && damagerRole == PlayerRole.SURVIVOR) {
                    e.setCancelled(true);
                    return;
                }

                if (victimRole == PlayerRole.SURVIVOR) {
                    // Patient-Zero and Corrupted can only damage survivors with their swords
                    Material mainHand = ((Player) damager).getInventory().getItemInMainHand().getType();
                    Material ptzSword = ItemDistributor.getPatientZeroSword().getType();
                    Material corruptedSword = ItemDistributor.getCorruptedSword().getType();

                    // Check if the patient-zero was holding his sword
                    if (damagerRole == PlayerRole.PATIENT_ZERO && mainHand != ptzSword) {
                        e.setCancelled(true);
                        return;
                    }

                    // Check if the corrupted player is holding his sword
                    if (damagerRole == PlayerRole.CORRUPTED && mainHand != corruptedSword) {
                        e.setCancelled(true);
                        return;
                    }

                    // Update the kill counter
                    GameSessionStats stats = ScoreboardSessionManager.getSession(mapName);
                    int currentKills = stats.getPlayerKills().getOrDefault(damager.getUniqueId(), 0);
                    stats.getPlayerKills().put(damager.getUniqueId(), currentKills + 1);

                    playerRoles.put(victim, PlayerRole.CORRUPTED); // Update the players role

                    if (!isRoleAlive(mapName, PlayerRole.SURVIVOR)) {
                        playerRoles.put(victim, PlayerRole.SURVIVOR); // Return the players old role to not win if he was a survivor
                        queue.setGameWinners(PlayerRole.CORRUPTED); // Set game winners as corrupted (and patient-zero)
                        QueueManager.gameEnd(queue, mapName);
                        return;
                    }

                    // Update to scoreboard corrupted count
                    stats.setCorruptedCount(stats.getCorruptedCount() + 1);
                    stats.setSurvivorsCount(stats.getSurvivorsCount() - 1);

                    // Start respawn cycle
                    corruptedRespawn(victim);
                    stats.getPlayerRoles().put(victim, PlayerRole.CORRUPTED);
                }
            }
        }
    }

    // Checks if a player with the given role is still alive
    public static boolean isRoleAlive(String mapName, PlayerRole role) {
        QueueInfo queue = QueueManager.getQueueInfo(mapName);

        if (queue == null || queue.getRoles() == null) return false;

        for (Map.Entry<Player, PlayerRole> entry : queue.getRoles().entrySet()) {
            Player player = entry.getKey();
            PlayerRole playerRole = entry.getValue();

            if (playerRole == role && player.isOnline() && !player.isDead()) {
                return true;
            }
        }

        return false;
    }

    // The respawn cycle for corrupted players
    public static void corruptedRespawn(Player player) {
        player.getInventory().clear(); // Clear the players currently applied layout
        if (player.getGameMode() != GameMode.SPECTATOR) player.setGameMode(GameMode.SPECTATOR);

        GameState gameState = QueueManager.getGameState(QueueManager.getMapOfPlayer(player));

        BukkitTask task = new BukkitRunnable() {
            int timeLeft = 5; // Time in seconds

            @Override
            public void run() {
                if (gameState == GameState.ENDING) {
                    player.setGameMode(GameMode.SURVIVAL);
                    cancel();
                    return;
                }

                if (timeLeft == 0) {
                    player.setGameMode(GameMode.SURVIVAL);
                    ItemDistributor.applyCorruptedLayout(player); // Apply layout for corrupted players
                    TeleportPlayers.teleportPlayerToCorruptedLocations(player); // Teleport player to a random spawn location
                    cancel(); // Cancel timer
                }

                // Display the player the time he has left until he respawns
                if (timeLeft <= 5 && timeLeft > 0) {
                    String title = config.getString("titles.roles.corrupted-respawn.title", "<red>You died!");
                    String subtitle = config.getString("titles.roles.corrupted-respawn.subtitle", "<yellow>Respawning in <red>%time%s<yellow>!").replace("%time%", String.valueOf(timeLeft));

                    MMUtils.displayTitle(player, title, subtitle, 0f, 1f, 0.3f); // Display the player the title
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0, 20); // Repeats every second
    }

    // Players can't die during the game
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (QueueManager.isPlayerQueued(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}

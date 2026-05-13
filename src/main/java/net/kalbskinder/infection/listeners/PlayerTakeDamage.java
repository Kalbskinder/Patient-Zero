package net.kalbskinder.infection.listeners;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.infection.Infection;
import net.kalbskinder.infection.enums.GameState;
import net.kalbskinder.infection.enums.PlayerRole;
import net.kalbskinder.infection.systems.ItemDistributor;
import net.kalbskinder.infection.systems.QueueInfo;
import net.kalbskinder.infection.systems.QueueManager;
import net.kalbskinder.infection.systems.TeleportPlayers;
import net.kalbskinder.infection.systems.scoreboard.GameSessionStats;
import net.kalbskinder.infection.systems.scoreboard.ScoreboardSessionManager;
import net.kalbskinder.infection.utils.MMUtils;
import net.kalbskinder.infection.utils.RoleUtils;
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

@RequiredArgsConstructor
public class PlayerTakeDamage implements Listener {
    private final FileConfiguration config;
    private final Infection plugin;
    private final QueueManager queueManager;
    private final ScoreboardSessionManager scoreboardSessionManager;
    private final ItemDistributor itemDistributor;
    private final TeleportPlayers teleportPlayers;
    private final RoleUtils roleUtils;

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        // Check if hurt entity is a player
        if (e.getEntity() instanceof Player)  {
            Player victim = (Player) e.getEntity(); // The player that got hurt
            Entity damager = e.getDamager(); // The Entity that hurt the player

            // Check if the player was queued
            if (!queueManager.isPlayerQueued(victim)) return;

            String mapName = queueManager.getMapOfPlayer(victim);
                QueueInfo queue = queueManager.getQueueInfo(mapName);

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

                    GameSessionStats stats = scoreboardSessionManager.getSession(mapName);

                    // Survivors can't hurt each other
                    if (victimRole == PlayerRole.SURVIVOR && shooterRole == PlayerRole.SURVIVOR) {
                        e.setCancelled(true);
                        return;
                    }

                    if (victimRole == PlayerRole.INFECTED && shooterRole == PlayerRole.SURVIVOR) {
                        // Update the kill counter
                        int currentKills = stats.getPlayerKills().getOrDefault(shooter.getUniqueId(), 0);
                        stats.getPlayerKills().put(shooter.getUniqueId(), currentKills + 1);

                        if (roleUtils.isRoleAlive(mapName, PlayerRole.ALPHA)) {
                            infectedRespawn(victim); // Start respawn cycle
                            return;
                        }

                        stats.setInfectedCount(stats.getInfectedCount() - 1);
                        playerRoles.remove(victim); // Player can no longer respawn

                        // Check if there's any other infected players left
                        if (!roleUtils.isRoleAlive(mapName, PlayerRole.INFECTED)) {
                            queue.setGameWinners(PlayerRole.SURVIVOR); // Set game winners as survivors
                                queueManager.gameEnd(queue, mapName);
                            return;
                        }

                        // Display player a title
                        String title = config.getString("titles.roles.final-death.title", "<red>You died!");
                        String subtitle = config.getString("titles.roles.final-death.subtitle", "<yellow>You can't respawn anymore!");
                        MMUtils.displayTitle(victim, title, subtitle, 1f, 3f, 1f);
                        victim.performCommand(config.getString("settings.executes.playerOnFinalDeath", "/me Teleport me!").substring(1));
                    }

                    // Only survivors can damage the alpha
                    if (victimRole == PlayerRole.ALPHA && shooterRole == PlayerRole.SURVIVOR) {
                        // Update the kill counter
                        int currentKills = stats.getPlayerKills().getOrDefault(shooter.getUniqueId(), 0);
                        stats.getPlayerKills().put(shooter.getUniqueId(), currentKills + 1);

                        // Check if there's any infected alive, else survivors win
                        if (!roleUtils.isRoleAlive(mapName, PlayerRole.INFECTED)) {
                            queue.setGameWinners(PlayerRole.SURVIVOR); // Set game winners as survivors
                            queueManager.gameEnd(queue, mapName); // End the game
                            return;
                        }

                        String title = config.getString("titles.roles.final-death.title", "<red>You died!");
                        MMUtils.displayTitle(victim, title, "", 1f, 3f, 1f);
                        victim.performCommand(config.getString("settings.executes.playerOnFinalDeath", "/me Teleport me!").substring(1));

                        // Read message from the config and notify players
                        queue.getPlayers().forEach(player -> {
                            MMUtils.sendMessage(player, config.getString("messages.alpha-dead", "<red><bold>Alpha died! <reset><red>It was %player%").replace("%player%", victim.getName()));
                            MMUtils.sendMessage(player, config.getString("message.alpha-dead-info", "<yellow>Infected players will no longer respawn!").replace("%player%", victim.getName()));
                            player.playSound(player.getLocation(), Sound.ENTITY_BAT_DEATH, 0.8f, 0.8f);
                        });

                        // Subtract the infected player count by one
                        stats.setInfectedCount(stats.getInfectedCount() - 1);

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

                // Alpha can't get damaged by another player directly
                if (victimRole == PlayerRole.ALPHA) {
                    e.setCancelled(true);
                    return;
                }

                if (victimRole == PlayerRole.INFECTED && damagerRole == PlayerRole.INFECTED) {
                    e.setCancelled(true);
                    return;
                }

                if (victimRole == PlayerRole.INFECTED && damagerRole == PlayerRole.ALPHA) {
                    e.setCancelled(true);
                    return;
                }

                // Survivors can only damage player with their bows
                if (victimRole == PlayerRole.INFECTED && damagerRole == PlayerRole.SURVIVOR) {
                    e.setCancelled(true);
                    return;
                }

                // Survivors can't damage each others
                if (victimRole == PlayerRole.SURVIVOR && damagerRole == PlayerRole.SURVIVOR) {
                    e.setCancelled(true);
                    return;
                }

                if (victimRole == PlayerRole.SURVIVOR) {
                    // Alpha and Infected can only damage survivors with their swords
                    Material mainHand = ((Player) damager).getInventory().getItemInMainHand().getType();
                    Material alphaSword = itemDistributor.getAlphaSword().getType();
                    Material infectedSword = itemDistributor.getInfectedSword().getType();

                    // Check if the alpha was holding his sword
                    if (damagerRole == PlayerRole.ALPHA && mainHand != alphaSword) {
                        e.setCancelled(true);
                        return;
                    }

                    // Check if the infected player is holding his sword
                    if (damagerRole == PlayerRole.INFECTED && mainHand != infectedSword) {
                        e.setCancelled(true);
                        return;
                    }

                    // Update the kill counter
                    GameSessionStats stats = scoreboardSessionManager.getSession(mapName);
                    int currentKills = stats.getPlayerKills().getOrDefault(damager.getUniqueId(), 0);
                    stats.getPlayerKills().put(damager.getUniqueId(), currentKills + 1);

                    playerRoles.put(victim, PlayerRole.INFECTED); // Update the players role

                    if (!roleUtils.isRoleAlive(mapName, PlayerRole.SURVIVOR)) {
                        playerRoles.put(victim, PlayerRole.SURVIVOR); // Return the players old role to not win if he was a survivor
                        queue.setGameWinners(PlayerRole.INFECTED); // Set game winners as infected (and alpha)
                        queueManager.gameEnd(queue, mapName);
                        return;
                    }

                    // Update to scoreboard infected count
                    stats.setInfectedCount(stats.getInfectedCount() + 1);
                    stats.setSurvivorsCount(stats.getSurvivorsCount() - 1);

                    // Start respawn cycle
                    infectedRespawn(victim);
                    stats.getPlayerRoles().put(victim, PlayerRole.INFECTED);
                }
            }
        }
    }

    // The respawn cycle for infected players
    private void infectedRespawn(Player player) {
        player.getInventory().clear(); // Clear the players currently applied layout
        if (player.getGameMode() != GameMode.SPECTATOR) player.setGameMode(GameMode.SPECTATOR);

        GameState gameState = queueManager.getGameState(queueManager.getMapOfPlayer(player));

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
                    itemDistributor.applyInfectedLayout(player); // Apply layout for infected players
                    teleportPlayers.teleportPlayerToInfectedLocations(player); // Teleport player to a random spawn location
                    cancel(); // Cancel timer
                }

                // Display the player the time he has left until he respawns
                if (timeLeft <= 5 && timeLeft > 0) {
                    String title = config.getString("titles.roles.infected-respawn.title", "<red>You died!");
                    String subtitle = config.getString("titles.roles.infected-respawn.subtitle", "<yellow>Respawning in <red>%time%s<yellow>!").replace("%time%", String.valueOf(timeLeft));

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
        if (queueManager.isPlayerQueued(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}

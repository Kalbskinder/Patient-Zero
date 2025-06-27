package me.kalbskinder.patientZero.systems;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.enums.GameState;
import me.kalbskinder.patientZero.enums.PlayerRole;
import me.kalbskinder.patientZero.listeners.PlayerTakeDamage;
import me.kalbskinder.patientZero.systems.scoreboard.GameSessionStats;
import me.kalbskinder.patientZero.systems.scoreboard.ScoreboardSessionManager;
import me.kalbskinder.patientZero.systems.scoreboard.ScoreboardUpdater;
import me.kalbskinder.patientZero.utils.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class QueueManager {
    private static final Map<String, QueueInfo> mapQueues = new HashMap<>();
    private static PatientZero plugin;

    public static void register(PatientZero main) {
        plugin = main;
    }

    private static final String customPrefix = Prefixes.getCustomPrefix();


    // Add a player to the queue hash map
    public static void addToQueue(String mapName, Player player) {
        QueueInfo queue = mapQueues.computeIfAbsent(mapName, m -> new QueueInfo());

        if (!queue.getPlayers().contains(player)) {
            queue.getPlayers().add(player);
            player.setGameMode(GameMode.ADVENTURE); // Set players game mode to adventure mode (can't break blocks)
        }

        FileConfiguration config = plugin.getConfig();

        ScoreboardUpdater.removeScoreboard(player);

        // Get all information about the quit-item
        String itemType = config.getString("settings.quit-item.item");
        String itemName = config.getString("settings.quit-item.name");
        List<String> itemLore = config.getStringList("settings.quit-item.lore");
        int amount = config.getInt("settings.quit-item.amount", 1);
        int slot = config.getInt("settings.quit-item.slot", 8);
        String actionId = player.getUniqueId() + "quit-item"; // Unique id for the click event

        player.getInventory().clear(); // Clear players inventory

        // Set right-click execution
        ItemActionHandler.registerAction(actionId, p -> {
            p.performCommand("ptz leave");
        });

        ItemStack item = ItemMaker.createItem(itemType, amount, itemName, itemLore, actionId); // Create the item
        ItemMaker.giveItemToPlayer(player, item, slot); // Give the player the item
        tryStartCountdown(mapName); // Try to start the queue countdown
    }

    // Remove a player from a queue
    public static boolean removePlayerFromAnyQueue(Player player) {
        String map = getMapOfPlayer(player);
        if (map != null) {
            QueueInfo queue = mapQueues.get(map);
            queue.getPlayers().removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));
            queue.getRoles().remove(player);
            player.getInventory().clear();
            player.setAllowFlight(false);

            // When game is still starting and a player left
            if (queue.getState() == GameState.STARTING && queue.getPlayers().size() < 2) {
                queue.setGameWinners(PlayerRole.SURVIVOR);
                queue.setState(GameState.ENDING);
                queue.getPlayers().forEach(p -> {
                    queue.getRoles().put(p, PlayerRole.SURVIVOR);
                });
                gameEnd(queue, map);
            }

            // Remove the scoreboard for the player
            ScoreboardUpdater.removeScoreboard(player);

            // Check if queue is currently in countdown
            // If less than 2 players notify last player
            if (queue.getState() == GameState.COUNTDOWN && queue.getPlayers().size() < 2) {
                if (queue.getCountdownTask() != null) queue.getCountdownTask().cancel();
                queue.setCountingDown(false); // Stop the countdown
                queue.setCountdownTask(null);
                queue.setState(GameState.WAITING); // Set mode back to waiting
                queue.getPlayers().forEach(p -> {
                    MMUtils.sendMessage(p, customPrefix + "<red>We don't have enough players! Start canceled.");
                });
            } else if (queue.getState() == GameState.INGAME) {

                // Check if there's enough players of a role to continue the game
                if (queue.getPlayers().size() < 2) {
                    // Game ends if there's less than 2 players
                    queue.setState(GameState.ENDING);

                    // Check which role should win
                    if (PlayerTakeDamage.isRoleAlive(map, PlayerRole.SURVIVOR)) {
                        queue.setGameWinners(PlayerRole.SURVIVOR);
                    } else {
                        queue.setGameWinners(PlayerRole.CORRUPTED);
                    }
                    gameEnd(queue, map);
                } else if (!PlayerTakeDamage.isRoleAlive(map, PlayerRole.SURVIVOR)) {
                    queue.setGameWinners(PlayerRole.CORRUPTED);
                    String role = plugin.getConfig().getString("roles.survivor");

                    // Send each player a message
                    queue.getPlayers().forEach(p -> {
                        MMUtils.sendMessage(p, customPrefix + "<yellow>The last " + role + " <yellow> left the game!");
                    });
                    gameEnd(queue, map);
                } else if (!PlayerTakeDamage.isRoleAlive(map, PlayerRole.CORRUPTED)) {
                    queue.setGameWinners(PlayerRole.SURVIVOR);

                    // Send each player a message
                    String role = plugin.getConfig().getString("roles.corrupted");
                    queue.getPlayers().forEach(p -> {
                        MMUtils.sendMessage(p, "<yellow>The last " + role + " <yellow>left the game!");
                    });

                    gameEnd(queue, map);
                }  else if (!PlayerTakeDamage.isRoleAlive(map, PlayerRole.CORRUPTED) &&
                        !PlayerTakeDamage.isRoleAlive(map, PlayerRole.PATIENT_ZERO)) {

                    queue.setGameWinners(PlayerRole.SURVIVOR);

                    queue.getPlayers().forEach(p -> {
                        MMUtils.sendMessage(p, customPrefix + "<yellow>All corrupted players have been eliminated!");
                    });

                    gameEnd(queue, map);
                }
            }
            return true;
        }
        return false;
    }

    public static List<Player> getQueue(String mapName) {
        QueueInfo queue = mapQueues.get(mapName);
        return queue != null ? queue.getPlayers() : new ArrayList<>();
    }

    public static GameState getGameState(String mapName) {
        QueueInfo queue = mapQueues.get(mapName);
        return queue != null ? queue.getState() : GameState.WAITING;
    }


    public static int getQueueSize(String mapName) {
        QueueInfo queue = mapQueues.get(mapName);
        return queue != null ? queue.getPlayers().size() : 0;
    }

    public static QueueInfo getQueueInfo(String mapName) {
        return mapQueues.get(mapName);
    }


    public static String getMapOfPlayer(Player player) {
        for (Map.Entry<String, QueueInfo> entry : mapQueues.entrySet()) {
            if (entry.getValue().getPlayers().contains(player)) {
                return entry.getKey();
            }
        }
        return null; // Player is not in queue
    }


    // Check if a player is queued
    public static boolean isPlayerQueued(Player player) {
        return mapQueues.values().stream().anyMatch(q -> q.getPlayers().contains(player));
    }

    // Queue countdown when there's at least two players in a queue
    public static void tryStartCountdown(String mapName) {
        QueueInfo queue = mapQueues.get(mapName);

        // Don't start the timer if the queue is already counting down
        // Needs at least 2 players to start
        if (queue == null || queue.getState() == GameState.COUNTDOWN || queue.getPlayers().size() < 2) return;

        // Read countdown message from config
        String countdownMessage = plugin.getConfig().getString(
                "messages.gamestart",
                "<yellow>Game starts in %time%<yellow>s"
        );

        queue.setState(GameState.COUNTDOWN); // Set map state to counting down

        BukkitTask task = new BukkitRunnable() {
            int timeLeft = 30;

            @Override
            public void run() {
                // Check if a player has left while the game was counting down
                if (queue.getPlayers().size() < 2) {
                    queue.setState(GameState.WAITING);
                    queue.setCountdownTask(null);
                    cancel(); // Cancel runnable

                    // Notify other players
                    queue.getPlayers().forEach(p -> MMUtils.sendMessage(p, customPrefix + "<red>We don't have enough players! Start canceled."));
                    return;
                }

                // Start the game when the timer hits 0
                if (timeLeft == 0) {
                    queue.getPlayers().forEach(p -> {
                        p.getInventory().clear(); // Clear inventory
                        p.setGameMode(GameMode.SURVIVAL); // Set game mode to survival
                        MMUtils.sendMessage(p, customPrefix + "<yellow>Game is starting");
                        gameStartCountdown(mapName, p);
                    });

                    // Teleport player to the map
                    TeleportPlayers.teleportPlayersOnGameStart(queue);
                    queue.setState(GameState.STARTING); // Set map state to starting

                    // Setup scoreboard
                    ScoreboardSessionManager.removeSession(mapName);
                    ScoreboardSessionManager.createSession(mapName);
                    GameSessionStats stats = ScoreboardSessionManager.getSession(mapName);

                    stats.setMapName(mapName);
                    stats.setTimer(plugin.getConfig().getInt("settings.gametime", 120));
                    stats.setCorruptedCount(1);
                    stats.setSurvivorsCount(queue.getPlayers().size() - 1);

                    // Set each players role to survivor and set kills to 0
                    for (Player p : queue.getPlayers()) {
                        stats.getPlayerRoles().put(p, PlayerRole.SURVIVOR);
                        stats.getPlayerKills().put(p, 0);
                    }

                    ScoreboardUpdater.startUpdater(mapName);

                    cancel();
                    return;
                }

                // Only send the game start message on these cases
                if (timeLeft == 30 || timeLeft == 10 || timeLeft == 5 || timeLeft <= 3) {
                    String color;
                    if (timeLeft <= 5) {
                        color = "<red>";
                    } else if (timeLeft == 10) {
                        color = "<gold>";
                    } else {
                        color = "<green>";
                    }

                    // Send the player a message with the time remaining
                    queue.getPlayers().forEach(p -> {
                        MMUtils.sendMessage(p, customPrefix + countdownMessage.replace("%time%", color + timeLeft));
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f); // Play a sound to the player
                    });

                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0, 20); // Repeats every second

        queue.setCountdownTask(task);
    }

    public static void gameEnd(QueueInfo queue, String mapName) {
        List<Player> players = queue.getPlayers();
        queue.setState(GameState.ENDING);

        // Get the game winners
        String gameWinners;
        if (queue.getGameWinners() == PlayerRole.SURVIVOR) {
            gameWinners = plugin.getConfig().getString("messages.winners.survivors", "<green>Survivors");
        } else {
            gameWinners = plugin.getConfig().getString("messages.winners.ptz", "<red>Corrupted <reset>& <red>Patient-Zero");
        }


        // Get the end message from the config
        List<String> endMessageLines = plugin.getConfig().getStringList("messages.end-message");
        GameSessionStats stats = ScoreboardSessionManager.getSession(mapName);

        BukkitTask task = new BukkitRunnable() {
            int timeLeft = 1; // 1 * 5s

            @Override
            public void run() {
                if (timeLeft == 1) {
                    timeLeft--;
                    players.forEach(p -> {
                        p.setGameMode(GameMode.ADVENTURE); // Set game mode back to adventure
                        p.setAllowFlight(false); // Remove double jumping

                        // Send the end message
                        endMessageLines.forEach(line -> {
                            String replacedLine = line.replace("%winners%", gameWinners);
                            int kills = stats.getPlayerKills().getOrDefault(p.getUniqueId(), 0);
                            replacedLine = replacedLine.replace("%kills%", String.valueOf(kills));

                            Component component = CenterTag.deserializeCentered(replacedLine);
                            MMUtils.sendMessageAsComponent(p, component); // Send centered message line
                        });


                        String winTitle = plugin.getConfig().getString("titles.win.title", "<green><bold>YOU WIN!<reset>");
                        String winSubtitle = plugin.getConfig().getString("titles.win.subtitle", " ");
                        String loseTitle = plugin.getConfig().getString("titles.lose.title", "<red><bold>YOU LOSE!<reset>");
                        String loseSubtitle = plugin.getConfig().getString("titles.lose.subtitle", " ");

                        Particle.DustOptions lime = new Particle.DustOptions(Color.LIME, 1.5F);
                        Particle.DustOptions red = new Particle.DustOptions(Color.RED, 1.5F);


                        // Display title, play sound & display particles, depending on winning or losing
                        if (queue.getRoles().get(p) == PlayerRole.SURVIVOR && queue.getGameWinners() == PlayerRole.SURVIVOR) {
                            MMUtils.displayTitle(p, winTitle, winSubtitle, 1f, 3f, 1f);
                            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 1.0f, 1.0f);
                            p.spawnParticle(Particle.DUST, p.getLocation().add(0, 0.5, 0), 80, 0.5, 0.5, 0.5, 0, lime);

                        } else if ((queue.getRoles().get(p) == PlayerRole.CORRUPTED || queue.getRoles().get(p) == PlayerRole.PATIENT_ZERO) &&
                                queue.getGameWinners() == PlayerRole.CORRUPTED) {
                            MMUtils.displayTitle(p, winTitle, winSubtitle, 1f, 3f, 1f);
                            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 1.0f, 1.0f);
                            p.spawnParticle(Particle.DUST, p.getLocation().add(0, 0.5, 0), 80, 0.5, 0.5, 0.5, 0, lime);

                        } else {
                            MMUtils.displayTitle(p, loseTitle, loseSubtitle, 1f, 3f, 1f);
                            p.playSound(p.getLocation(), Sound.ENTITY_BREEZE_DEATH, 1.0f, 0.8f);
                            p.spawnParticle(Particle.DUST, p.getLocation().add(0, 0.5, 0), 80, 0.5, 0.5, 0.5, 0, red);
                        }

                        p.getInventory().clear();
                    });
                } else {
                    // Create copy of playlist to avoid ConcurrentModificationException
                    List<Player> finalPlayers = new ArrayList<>(QueueManager.getQueue(mapName));
                    finalPlayers.forEach(p -> {
                        if (QueueManager.isPlayerQueued(p)) {
                            QueueManager.removePlayerFromAnyQueue(p);
                            String command = plugin.getConfig().getString("settings.executes.playerOnGameEnd");
                            command = command.substring(1); // remove '/' from the start of the command
                            p.performCommand(command);
                        }
                    });
                    ScoreboardSessionManager.removeSession(mapName);
                    queue.setState(GameState.WAITING); // Open queue for others to join again
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 100); // 5 seconds = 20*5 ticks
    }

    // 9 seconds countdown before the roles are being assigned
    public static void gameStartCountdown(String mapName, Player p) {
        BukkitTask task = new BukkitRunnable() {
            int timeLeft = 9; // Time in seconds

            @Override
            public void run() {
                QueueInfo queue = QueueManager.getQueueInfo(mapName);
                if (queue.getPlayers().size() < 2) {
                    cancel();
                    return;
                }

                if (!isPlayerQueued(p)) {
                    cancel();
                    return;
                }

                if (timeLeft == 0) {
                    assignRoles(mapName); // Assign all player's their role
                    queue.setState(GameState.INGAME); // Set gamestate to ingame
                    cancel(); // Cancel timer
                }

                if (timeLeft <= 5 && timeLeft > 0) {
                    // Read message from config and send to player
                    String message = plugin.getConfig().getString("messages.roleassign", "<yellow>Roles are assigned in <red>%time%<yellow>s");
                    MMUtils.sendMessage(p, message.replace("%time%", String.valueOf(timeLeft)));
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f); // Play a sound to the player
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0, 20); // Repeats every second
    }

    public static void assignRoles(String mapName) {
        QueueInfo queue = mapQueues.get(mapName);

        if (queue.getState() != GameState.INGAME) return; // Only assign roles if game is still ongoing

        // Skip if roles already assigned (this function gets called by each player but is only needed once)
        if (!queue.getRoles().isEmpty()) return;

        queue.getRoles().clear(); // Reset all player's roles from previous games

        List<Player> players = queue.getPlayers();

        if (players.size() < 1) return;

        // Pick a random player to be the patient-zero
        Player patientZero = players.get(new Random().nextInt(players.size()));
        queue.getRoles().put(patientZero, PlayerRole.PATIENT_ZERO);

        // All other players will be survivors
        for (Player player : players) {
            if (!player.equals(patientZero)) {
                queue.getRoles().put(player, PlayerRole.SURVIVOR);
            }

        }

        // Update scoreboard with the new assigned roles
        GameSessionStats stats = ScoreboardSessionManager.getSession(mapName);
        stats.setPlayerRoles(queue.getRoles());

        // Display to each player the role they received
        players.forEach(p -> {
            PlayerRole role = queue.getRoles().get(p);
            FileConfiguration config = plugin.getConfig();

            if (config.getBoolean("settings.double-jump.enabled")) {
                p.setAllowFlight(true);
            } else {
                p.setAllowFlight(false);
            }

            // Read title from config
            String basepath = (role == PlayerRole.SURVIVOR ? "titles.roles.survivor" : "titles.roles.patientzero");
            String title = config.getString(basepath + ".title");
            String subtitle = config.getString(basepath + ".subtitle");

            // Display title
            MMUtils.displayTitle(p, title, subtitle, 1f, 3f, 1f);

            // Apply inventory layout
            ItemDistributor.applyRoleLayout(p, role);
        });
    }

}

package net.kalbskinder.infection.systems;

import lombok.Getter;
import net.kalbskinder.infection.Infection;
import net.kalbskinder.infection.enums.GameState;
import net.kalbskinder.infection.enums.PlayerRole;
import net.kalbskinder.infection.systems.scoreboard.GameSessionStats;
import net.kalbskinder.infection.systems.scoreboard.ScoreboardSessionManager;
import net.kalbskinder.infection.systems.scoreboard.ScoreboardUpdater;
import net.kalbskinder.infection.utils.*;
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
    private final Infection plugin;
    private final ItemActionHandler itemActionHandler;
    private final ItemMaker itemMaker;
    private final ItemDistributor itemDistributor;

    @Getter private final ScoreboardSessionManager scoreboardSessionManager;
    @Getter private final ScoreboardUpdater scoreboardUpdater;
    @Getter private final TeleportPlayers teleportPlayers;
    @Getter private final RoleUtils roleUtils;

    private final Map<String, QueueInfo> mapQueues = new HashMap<>();
    private static final String CUSTOM_PREFIX = Prefixes.getCustomPrefix();

    public QueueManager(Infection plugin, ItemActionHandler itemActionHandler, ItemMaker itemMaker, ItemDistributor itemDistributor) {
        this.plugin = plugin;
        this.itemActionHandler = itemActionHandler;
        this.itemMaker = itemMaker;
        this.itemDistributor = itemDistributor;
        this.scoreboardSessionManager = new ScoreboardSessionManager();
        this.scoreboardUpdater = new ScoreboardUpdater(plugin, this, scoreboardSessionManager);
        this.teleportPlayers = new TeleportPlayers(plugin.getConfig(), this);
        this.roleUtils = new RoleUtils(this);
    }

    // Add a player to the queue hash map
    public void addToQueue(String mapName, Player player) {
        QueueInfo queue = mapQueues.computeIfAbsent(mapName, m -> new QueueInfo());

        if (!queue.getPlayers().contains(player)) {
            queue.getPlayers().add(player);
            player.setGameMode(GameMode.ADVENTURE); // Set players game mode to adventure mode (can't break blocks)
        }

        FileConfiguration config = plugin.getConfig();

        scoreboardUpdater.removeScoreboard(player);

        // Get all information about the quit-item
        String itemType = config.getString("settings.quit-item.item");
        String itemName = config.getString("settings.quit-item.name");
        List<String> itemLore = config.getStringList("settings.quit-item.lore");
        int amount = config.getInt("settings.quit-item.amount", 1);
        int slot = config.getInt("settings.quit-item.slot", 8);
        String actionId = player.getUniqueId() + "quit-item"; // Unique id for the click event

        player.getInventory().clear(); // Clear players inventory

        // Set right-click execution
        itemActionHandler.registerAction(actionId, p -> {
            p.performCommand("infection leave");
        });

        ItemStack item = itemMaker.createItem(itemType, amount, itemName, itemLore, actionId); // Create the item
        itemMaker.giveItemToPlayer(player, item, slot); // Give the player the item
        tryStartCountdown(mapName); // Try to start the queue countdown
    }

    // Remove a player from a queue
    public boolean removePlayerFromAnyQueue(Player player) {
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
            scoreboardUpdater.removeScoreboard(player);

            // Check if queue is currently in countdown
            // If less than 2 players notify last player
            if (queue.getState() == GameState.COUNTDOWN && queue.getPlayers().size() < 2) {
                if (queue.getCountdownTask() != null) queue.getCountdownTask().cancel();
                queue.setCountingDown(false); // Stop the countdown
                queue.setCountdownTask(null);
                queue.setState(GameState.WAITING); // Set mode back to waiting
                queue.getPlayers().forEach(p -> {
                    MMUtils.sendMessage(p, CUSTOM_PREFIX + "<red>We don't have enough players! Start canceled.");
                });
            } else if (queue.getState() == GameState.INGAME) {

                // Check if there's enough players of a role to continue the game
                if (queue.getPlayers().size() < 2) {
                    // Game ends if there's less than 2 players
                    queue.setState(GameState.ENDING);

                    // Check which role should win
                    if (roleUtils.isRoleAlive(map, PlayerRole.SURVIVOR)) {
                        queue.setGameWinners(PlayerRole.SURVIVOR);
                    } else {
                        queue.setGameWinners(PlayerRole.INFECTED);
                    }
                    gameEnd(queue, map);
                } else if (!roleUtils.isRoleAlive(map, PlayerRole.SURVIVOR)) {
                    queue.setGameWinners(PlayerRole.INFECTED);
                    String role = plugin.getConfig().getString("roles.survivor");

                    // Send each player a message
                    queue.getPlayers().forEach(p -> {
                        MMUtils.sendMessage(p, CUSTOM_PREFIX + "<yellow>The last " + role + " <yellow> left the game!");
                    });
                    gameEnd(queue, map);
                } else if (!roleUtils.isRoleAlive(map, PlayerRole.INFECTED)) {
                    queue.setGameWinners(PlayerRole.SURVIVOR);

                    // Send each player a message
                    String role = plugin.getConfig().getString("roles.infected");
                    queue.getPlayers().forEach(p -> {
                        MMUtils.sendMessage(p, "<yellow>The last " + role + " <yellow>left the game!");
                    });

                    gameEnd(queue, map);
                }  else if (!roleUtils.isRoleAlive(map, PlayerRole.INFECTED) &&
                        !roleUtils.isRoleAlive(map, PlayerRole.ALPHA)) {

                    queue.setGameWinners(PlayerRole.SURVIVOR);

                    queue.getPlayers().forEach(p -> {
                        MMUtils.sendMessage(p, CUSTOM_PREFIX + "<yellow>All infected players have been eliminated!");
                    });

                    gameEnd(queue, map);
                }
            }
            return true;
        }
        return false;
    }

    public List<Player> getQueue(String mapName) {
        QueueInfo queue = mapQueues.get(mapName);
        return queue != null ? queue.getPlayers() : new ArrayList<>();
    }

    public GameState getGameState(String mapName) {
        QueueInfo queue = mapQueues.get(mapName);
        return queue != null ? queue.getState() : GameState.WAITING;
    }


    public int getQueueSize(String mapName) {
        QueueInfo queue = mapQueues.get(mapName);
        return queue != null ? queue.getPlayers().size() : 0;
    }

    public QueueInfo getQueueInfo(String mapName) {
        return mapQueues.get(mapName);
    }


    public String getMapOfPlayer(Player player) {
        for (Map.Entry<String, QueueInfo> entry : mapQueues.entrySet()) {
            if (entry.getValue().getPlayers().contains(player)) {
                return entry.getKey();
            }
        }
        return null; // Player is not in queue
    }


    // Check if a player is queued
    public boolean isPlayerQueued(Player player) {
        return mapQueues.values().stream().anyMatch(q -> q.getPlayers().contains(player));
    }

    // Queue countdown when there's at least two players in a queue
    public void tryStartCountdown(String mapName) {
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
                    queue.getPlayers().forEach(p -> MMUtils.sendMessage(p, CUSTOM_PREFIX + "<red>We don't have enough players! Start canceled."));
                    return;
                }

                // Start the game when the timer hits 0
                if (timeLeft == 0) {
                    queue.getPlayers().forEach(p -> {
                        p.getInventory().clear(); // Clear inventory
                        p.setGameMode(GameMode.SURVIVAL); // Set game mode to survival
                        MMUtils.sendMessage(p, CUSTOM_PREFIX + "<yellow>Game is starting");
                        gameStartCountdown(mapName, p);
                    });

                    // Teleport player to the map
                    teleportPlayers.teleportPlayersOnGameStart(queue);
                    queue.setState(GameState.STARTING); // Set map state to starting

                    // Setup scoreboard
                    scoreboardSessionManager.removeSession(mapName);
                    scoreboardSessionManager.createSession(mapName);
                    GameSessionStats stats = scoreboardSessionManager.getSession(mapName);

                    stats.setMapName(mapName);
                    stats.setTimer(plugin.getConfig().getInt("settings.gametime", 120));
                    stats.setInfectedCount(1);
                    stats.setSurvivorsCount(queue.getPlayers().size() - 1);

                    // Set each players role to survivor and set kills to 0
                    for (Player p : queue.getPlayers()) {
                        stats.getPlayerRoles().put(p, PlayerRole.SURVIVOR);
                        stats.getPlayerKills().put(p.getUniqueId(), 0);
                    }

                    scoreboardUpdater.startUpdater(mapName);

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
                        MMUtils.sendMessage(p, CUSTOM_PREFIX + countdownMessage.replace("%time%", color + timeLeft));
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f); // Play a sound to the player
                    });

                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0, 20); // Repeats every second

        queue.setCountdownTask(task);
    }

    public void gameEnd(QueueInfo queue, String mapName) {
        List<Player> players = queue.getPlayers();
        queue.setState(GameState.ENDING);

        // Get the game winners
        String gameWinners;
        if (queue.getGameWinners() == PlayerRole.SURVIVOR) {
            gameWinners = plugin.getConfig().getString("messages.winners.survivors", "<green>Survivors");
        } else {
            gameWinners = plugin.getConfig().getString("messages.winners.infected", "<red>Infected <reset>& <red>Alpha");
        }


        // Get the end message from the config
        List<String> endMessageLines = plugin.getConfig().getStringList("messages.end-message");
        GameSessionStats stats = scoreboardSessionManager.getSession(mapName);

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

                        } else if ((queue.getRoles().get(p) == PlayerRole.INFECTED || queue.getRoles().get(p) == PlayerRole.ALPHA) &&
                                queue.getGameWinners() == PlayerRole.INFECTED) {
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
                    List<Player> finalPlayers = new ArrayList<>(getQueue(mapName));
                    finalPlayers.forEach(p -> {
                        if (isPlayerQueued(p)) {
                            removePlayerFromAnyQueue(p);
                            String command = plugin.getConfig().getString("settings.executes.playerOnGameEnd");
                            command = command.substring(1); // remove '/' from the start of the command
                            p.performCommand(command);
                        }
                    });
                    scoreboardSessionManager.removeSession(mapName);
                    queue.setState(GameState.WAITING); // Open queue for others to join again
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 100); // 5 seconds = 20*5 ticks
    }

    // 9 seconds countdown before the roles are being assigned
    public void gameStartCountdown(String mapName, Player p) {
        BukkitTask task = new BukkitRunnable() {
            int timeLeft = 9; // Time in seconds

            @Override
            public void run() {
                QueueInfo queue = getQueueInfo(mapName);
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

    public void assignRoles(String mapName) {
        QueueInfo queue = mapQueues.get(mapName);

        if (queue.getState() != GameState.INGAME) return; // Only assign roles if game is still ongoing

        // Skip if roles already assigned (this function gets called by each player but is only needed once)
        if (!queue.getRoles().isEmpty()) return;

        queue.getRoles().clear(); // Reset all player's roles from previous games

        List<Player> players = queue.getPlayers();

        if (players.size() < 1) return;

        // Pick a random player to be the alpha
        Player alpha = players.get(new Random().nextInt(players.size()));
        queue.getRoles().put(alpha, PlayerRole.ALPHA);

        // All other players will be survivors
        for (Player player : players) {
            if (!player.equals(alpha)) {
                queue.getRoles().put(player, PlayerRole.SURVIVOR);
            }

        }

        // Update scoreboard with the new assigned roles
        GameSessionStats stats = scoreboardSessionManager.getSession(mapName);
        stats.setPlayerRoles(queue.getRoles());

        // Display to each player the role they received
        players.forEach(p -> {
            PlayerRole role = queue.getRoles().get(p);
            FileConfiguration config = plugin.getConfig();

            // Read title from config
            String basepath = (role == PlayerRole.SURVIVOR ? "titles.roles.survivor" : "titles.roles.alpha");
            String title = config.getString(basepath + ".title");
            String subtitle = config.getString(basepath + ".subtitle");

            // Display title
            MMUtils.displayTitle(p, title, subtitle, 1f, 3f, 1f);

            // Apply inventory layout
            itemDistributor.applyRoleLayout(p, role);
        });
    }

}

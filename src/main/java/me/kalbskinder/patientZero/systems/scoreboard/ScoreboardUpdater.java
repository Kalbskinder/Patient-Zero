package me.kalbskinder.patientZero.systems.scoreboard;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.enums.GameState;
import me.kalbskinder.patientZero.enums.PlayerRole;
import me.kalbskinder.patientZero.systems.QueueInfo;
import me.kalbskinder.patientZero.systems.QueueManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.List;
import java.util.Map;

public class ScoreboardUpdater {
    private static FileConfiguration config;
    private static PatientZero plugin;
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();

    public static void register(PatientZero main) {
        plugin = main;
        config = main.getConfig();
    }

    private static int timer = 5;

    // Updater to update the scoreboard all 4 ticks
    public static void startUpdater(String mapName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                QueueInfo queue = QueueManager.getQueueInfo(mapName);

                if (queue == null) {
                    return;
                }

                GameState gameState = queue.getState();

                // Stop updating the scoreboard after the game ended or is waiting
                if (gameState == GameState.ENDING || gameState == GameState.WAITING) {
                    cancel();
                    return;
                }

                // Update the scoreboard
                for (Player player : queue.getPlayers()) {
                    updateScoreboardForPlayer(player, queue);
                }

                updateTimer(mapName);
            }
        }.runTaskTimer(plugin, 0, 4); // All 4 Ticks (0.2s)
    }

    public static void updateScoreboardForPlayer(Player player, QueueInfo queue) {
        String name = config.getString("scoreboard.title", "<yellow><bold>Scoreboard<reset>");
        List<String> lines = config.getStringList("scoreboard.lines");

        String mapName = QueueManager.getMapOfPlayer(queue.getPlayers().getFirst());
        GameSessionStats stats = ScoreboardSessionManager.getSession(mapName);

        // Get the player role
        Map<Player, PlayerRole> playerRoles = stats.getPlayerRoles();
        PlayerRole role = playerRoles.get(player);


        // Read configured role names from the config
        String playerRole;

        if (role == null) {
            playerRole = "<dark_red>Unkown";
        } else if (role == PlayerRole.PATIENT_ZERO) {
            playerRole = config.getString("roles.patientzero", "<red>Patient Zero");
        } else if (role == PlayerRole.CORRUPTED) {
            playerRole = config.getString("roles.corrupted", "<red>Corrupted");
        } else {
            playerRole = config.getString("roles.survivor", "<green>Survivor");
        }

        // Get other stats
        int survivors = stats.getSurvivorsCount();
        int corrupted = stats.getCorruptedCount();
        String timer = formatSeconds(stats.getTimer());
        int kills = stats.getPlayerKills().getOrDefault(player, 0);
        String map = QueueManager.getMapOfPlayer(player);

        if (lines.isEmpty()) {
            lines = List.of("<gray>Scoreboard empty");
        }

        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = sb.registerNewObjective("dummy", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(legacy.serialize(mm.deserialize(name)));

        int score = lines.size();
        for (String line : lines) {
            String formatted = line
                    .replace("%player%", player.getName())
                    .replace("%role%", playerRole)
                    .replace("%survivors%", String.valueOf(survivors))
                    .replace("%corrupted%", String.valueOf(corrupted))
                    .replace("%timer%", timer)
                    .replace("%kills%", String.valueOf(kills))
                    .replace("%map%", map);

            formatted = legacy.serialize(mm.deserialize(formatted));
            obj.getScore(formatted).setScore(score--);
        }

        player.setScoreboard(sb);
    }

    // Format the seconds left into a readable format
    private static String formatSeconds(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%d:%02d", min, sec);
    }

    // Updates the timer all 5*4 ticks (every second)
    private static void updateTimer(String mapName) {
        GameSessionStats stats = ScoreboardSessionManager.getSession(mapName);

        if (stats == null) return;

        if (timer == 0) {
            if (stats.getTimer() >=1) {
                stats.setTimer(stats.getTimer() - 1);
            } else {
                QueueInfo queue = QueueManager.getQueueInfo(mapName);
                queue.setGameWinners(PlayerRole.SURVIVOR);
                QueueManager.gameEnd(queue, mapName);
            }
            timer = 5;
        }
        timer--;
    }

    // Remove the scoreboard from the player
    public static void removeScoreboard(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            return;
        }

        Scoreboard mainScoreboard = manager.getMainScoreboard();
        player.setScoreboard(mainScoreboard); // Update the scoreboard
    }
}


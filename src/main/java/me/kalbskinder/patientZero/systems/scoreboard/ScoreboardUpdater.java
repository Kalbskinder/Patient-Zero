package me.kalbskinder.patientZero.systems.scoreboard;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.enums.GameState;
import me.kalbskinder.patientZero.enums.PlayerRole;
import me.kalbskinder.patientZero.systems.QueueInfo;
import me.kalbskinder.patientZero.systems.QueueManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ScoreboardUpdater {
    private static FileConfiguration config;
    private static PatientZero plugin;

    public static void register(PatientZero main) {
        plugin = main;
        config = main.getConfig();
    }

    private static int timer = 5;

    public static void startUpdater(String mapName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                QueueInfo queue = QueueManager.getQueueInfo(mapName);

                if (queue == null) {
                    return;
                }

                GameState gameState = queue.getState();

                if (gameState == GameState.ENDING || gameState == GameState.WAITING) {
                    cancel();
                    return;
                }

                for (Player player : queue.getPlayers()) {
                    updateScoreboardForPlayer(player, queue);
                }

                updateTimer(mapName);
            }
        }.runTaskTimer(plugin, 0, 4); // All 4 Ticks
    }

    public static void updateScoreboardForPlayer(Player player, QueueInfo queue) {
        String name = config.getString("scoreboard.title", "§6§lScoreboard");
        List<String> lines = config.getStringList("scoreboard.lines");

        String mapName = QueueManager.getMapOfPlayer(queue.getPlayers().getFirst());
        GameSessionStats stats = ScoreboardSessionManager.getSession(mapName);

        // Get the player role
        Map<Player, PlayerRole> playerRoles = stats.getPlayerRoles();
        PlayerRole role = playerRoles.get(player);


        // Turn the player role into a string
        String playerRole;

        if (role == null) {
            playerRole = "§4Unkown";
        } else if (role == PlayerRole.PATIENT_ZERO) {
            playerRole = config.getString("roles.patientzero");
        } else if (role == PlayerRole.CORRUPTED) {
            playerRole = config.getString("roles.corrupted");
        } else {
            playerRole = config.getString("roles.survivor");
        }

        // Get other stats
        int survivors = stats.getSurvivorsCount();
        int corrupted = stats.getCorruptedCount();
        String timer = formatSeconds(stats.getTimer());
        int kills = stats.getPlayerKills().getOrDefault(player, 0);
        String map = QueueManager.getMapOfPlayer(player);

        if (lines.isEmpty()) {
            lines = List.of("§7Scoreboard empty");
        }

        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = sb.registerNewObjective("dummy", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(name);

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

            obj.getScore(formatted).setScore(score--);
        }

        player.setScoreboard(sb);
    }

    private static String formatSeconds(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%d:%02d", min, sec);
    }

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


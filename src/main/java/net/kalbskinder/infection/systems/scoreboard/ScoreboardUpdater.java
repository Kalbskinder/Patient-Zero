package net.kalbskinder.infection.systems.scoreboard;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.infection.Infection;
import net.kalbskinder.infection.enums.GameState;
import net.kalbskinder.infection.enums.PlayerRole;
import net.kalbskinder.infection.systems.QueueInfo;
import net.kalbskinder.infection.systems.QueueManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ScoreboardUpdater {
    private final Infection plugin;
    private final QueueManager queueManager;
    private final ScoreboardSessionManager scoreboardSessionManager;
    
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static int TIMER = 5;

    // Updater to update the scoreboard all 4 ticks
    public void startUpdater(String mapName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                QueueInfo queue = queueManager.getQueueInfo(mapName);

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

    public void updateScoreboardForPlayer(Player player, QueueInfo queue) {
        FileConfiguration config = plugin.getConfig();
        String name = config.getString("scoreboard.title", "<yellow><bold>Scoreboard<reset>");
        List<String> lines = config.getStringList("scoreboard.lines");

        String mapName = queueManager.getMapOfPlayer(queue.getPlayers().getFirst());
        GameSessionStats stats = scoreboardSessionManager.getSession(mapName);

        // Get the player role
        Map<Player, PlayerRole> playerRoles = stats.getPlayerRoles();
        PlayerRole role = playerRoles.get(player);


        // Read configured role names from the config
        String playerRole;

        if (role == null) {
            playerRole = "<dark_red>Unkown";
        } else if (role == PlayerRole.ALPHA) {
            playerRole = config.getString("roles.alpha", "<red>Alpha");
        } else if (role == PlayerRole.INFECTED) {
            playerRole = config.getString("roles.infected", "<red>Infected");
        } else {
            playerRole = config.getString("roles.survivor", "<green>Survivor");
        }

        // Get other stats
        int survivors = stats.getSurvivorsCount();
        int infected = stats.getInfectedCount();
        String timer = formatSeconds(stats.getTimer());
        int kills = stats.getPlayerKills().getOrDefault(player.getUniqueId(), 0);
        String map = queueManager.getMapOfPlayer(player);

        if (lines.isEmpty()) {
            lines = List.of("<gray>Scoreboard empty");
        }

        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = sb.registerNewObjective("dummy", Criteria.DUMMY, MM.deserialize(name));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.displayName(MM.deserialize(name));

        int score = lines.size();
        for (String line : lines) {
            String formatted = line
                    .replace("%player%", player.getName())
                    .replace("%role%", playerRole)
                    .replace("%survivors%", String.valueOf(survivors))
                    .replace("%infected%", String.valueOf(infected))
                    .replace("%timer%", timer)
                    .replace("%kills%", String.valueOf(kills))
                    .replace("%map%", map);

            formatted = LEGACY.serialize(MM.deserialize(formatted));
            obj.getScore(formatted).setScore(score--);
        }

        player.setScoreboard(sb);
    }

    // Format the seconds left into a readable format
    private String formatSeconds(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%d:%02d", min, sec);
    }

    // Updates the timer all 5*4 ticks (every second)
    private void updateTimer(String mapName) {
        GameSessionStats stats = scoreboardSessionManager.getSession(mapName);

        if (stats == null) return;

        if (TIMER == 0) {
            if (stats.getTimer() >=1) {
                stats.setTimer(stats.getTimer() - 1);
            } else {
                QueueInfo queue = queueManager.getQueueInfo(mapName);
                queue.setGameWinners(PlayerRole.SURVIVOR);
                queueManager.gameEnd(queue, mapName);
            }
            TIMER = 5;
        }
        TIMER--;
    }

    // Remove the scoreboard from the player
    public void removeScoreboard(Player player) {
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


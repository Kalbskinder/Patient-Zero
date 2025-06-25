package me.kalbskinder.patientZero.systems;

import me.kalbskinder.patientZero.enums.GameState;
import me.kalbskinder.patientZero.enums.PlayerRole;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueueInfo {
    private List<Player> players = new ArrayList<>();
    private BukkitTask countdownTask;
    private boolean isCountingDown = false;
    private GameState state = GameState.WAITING;
    private PlayerRole gameWinners;
    private final Map<Player, PlayerRole> roles = new HashMap<>();

    public Map<Player, PlayerRole> getRoles() {
        return roles;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public void setGameWinners(PlayerRole gameWinners) {
        this.gameWinners = gameWinners;
    }

    public PlayerRole getGameWinners() {
        return gameWinners;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isCountingDown() {
        return isCountingDown;
    }

    public void setCountingDown(boolean countingDown) {
        isCountingDown = countingDown;
    }

    public BukkitTask getCountdownTask() {
        return countdownTask;
    }

    public void setCountdownTask(BukkitTask countdownTask) {
        this.countdownTask = countdownTask;
    }
}

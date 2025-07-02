package me.kalbskinder.patientZero.systems.scoreboard;

import me.kalbskinder.patientZero.enums.PlayerRole;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameSessionStats {
    private int timer;
    private int survivorsCount;
    private int corruptedCount;
    private String mapName;
    private final Map<UUID, Integer> playerKills = new HashMap<>();
    private final Map<Player, PlayerRole> playerRoles = new HashMap<>();

    public Map<UUID, Integer> getPlayerKills() {
        return playerKills;
    }

    public Map<Player, PlayerRole> getPlayerRoles() {
        return playerRoles;
    }

    public void setPlayerRoles(Map<Player, PlayerRole> playerRoles) {
        this.playerRoles.clear();
        this.playerRoles.putAll(playerRoles);
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public int getSurvivorsCount() {
        return survivorsCount;
    }

    public void setSurvivorsCount(int survivorsCount) {
        this.survivorsCount = survivorsCount;
    }

    public int getCorruptedCount() {
        return corruptedCount;
    }

    public void setCorruptedCount(int corruptedCount) {
        this.corruptedCount = corruptedCount;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
}

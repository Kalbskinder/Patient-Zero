package me.kalbskinder.patientZero.systems.scoreboard;

import me.kalbskinder.patientZero.enums.PlayerRole;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GameSessionStats {
    private int timer;
    private int survivorsCount;
    private int corruptedCount;
    private String mapName;
    private final Map<Player, Integer> playerKills = new HashMap<>();
    private final Map<Player, PlayerRole> playerRoles = new HashMap<>();

    public Map<Player, Integer> getPlayerKills() {
        return playerKills;
    }

    public void setPlayerKills(Map<Player, Integer> playerKills) {
        this.playerKills.clear();
        this.playerKills.putAll(playerKills);
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

package net.kalbskinder.infection.systems.scoreboard;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardSessionManager {
    private final Map<String, GameSessionStats> activeSessions = new HashMap<>();

    public GameSessionStats getSession(String mapName) {
        return activeSessions.get(mapName);
    }

    public void createSession(String mapName) {
        activeSessions.put(mapName, new GameSessionStats());
    }

    public void removeSession(String mapName) {
        activeSessions.remove(mapName);
    }
}


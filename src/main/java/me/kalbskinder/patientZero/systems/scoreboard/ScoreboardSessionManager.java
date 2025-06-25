package me.kalbskinder.patientZero.systems.scoreboard;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardSessionManager {
    private static final Map<String, GameSessionStats> activeSessions = new HashMap<>();

    public static GameSessionStats getSession(String mapName) {
        return activeSessions.get(mapName);
    }

    public static void createSession(String mapName) {
        activeSessions.put(mapName, new GameSessionStats());
    }

    public static void removeSession(String mapName) {
        activeSessions.remove(mapName);
    }
}


package me.kalbskinder.patientZero.enums;

public enum GameState {
    WAITING,     // Queue is open to join
    COUNTDOWN,   // Countdown for game start has started
    STARTING,    // Countdown ingame before the roles are assigned
    INGAME,      // Game has started, role distribution
    ENDING       // A winner has been chosen, final game message, teleport players out of the map
}
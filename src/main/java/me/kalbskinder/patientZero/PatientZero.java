package me.kalbskinder.patientZero;

import me.kalbskinder.patientZero.listeners.*;
import me.kalbskinder.patientZero.systems.ItemDistributor;
import me.kalbskinder.patientZero.systems.QueueManager;
import me.kalbskinder.patientZero.systems.TeleportPlayers;
import me.kalbskinder.patientZero.systems.scoreboard.ScoreboardUpdater;
import me.kalbskinder.patientZero.utils.ItemActionHandler;
import me.kalbskinder.patientZero.utils.PlayerCheck;
import me.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import me.kalbskinder.patientZero.commands.BaseCommand;

import java.util.logging.Logger;

public final class PatientZero extends JavaPlugin {

    // Register event listeners
    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerQuit(), this); // Player quit event
        pm.registerEvents(new PlayerMove(), this); // Player move event
        pm.registerEvents(new ItemActionHandler(), this); // Item right-click event
        pm.registerEvents(new PlayerDropItem(), this); // Drop item event
        pm.registerEvents(new PlayerChangeWorld(), this); // Player change world event
        pm.registerEvents(new PlayerTakeDamage(this), this); // Player takes damage, handles respawn and game end mechanics
        pm.registerEvents(new EntityShootArrow(), this); // Entity shoot arrow event, makes it so players can't pick up the arrow
        pm.registerEvents(new PlayerBreakBlock(), this); // Player break block event, cancel event if player is queued
        pm.registerEvents(new DoubleJumpListener(this), this); // Listener when player trys double jumping
    }

    // Pass the plugin instance to the methods
    private void registerMethods() {
        TeleportPlayers.register(this);
        Prefixes.register(this);
        QueueManager.register(this);
        PlayerCheck.register(this);
        ItemDistributor.register(this);
        ScoreboardUpdater.register(this);
    }

    private void startUpMessage() {
        Logger logger = Logger.getLogger("PTZ");
        logger.info("-------------------------------");
        logger.info("        PTZ - Patient Zero       ");
        logger.info("       Version: " + getDescription().getVersion());
        logger.info("       Author: " + getDescription().getAuthors().getFirst());
        logger.info("--------------------------------");
    }

    @Override
    public void onEnable() {
        startUpMessage();

        // Plugin startup logic
        saveDefaultConfig(); // Save default config file (config.yml)
        getCommand("ptz").setExecutor(new BaseCommand(this)); // Register the '/ptz' command

        registerListeners();  // Register event listeners
        registerMethods(); // Register methods
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

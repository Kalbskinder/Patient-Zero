package net.kalbskinder.patientZero;

import net.kalbskinder.patientZero.listeners.*;
import net.kalbskinder.patientZero.systems.ItemDistributor;
import net.kalbskinder.patientZero.systems.QueueManager;
import net.kalbskinder.patientZero.systems.TeleportPlayers;
import net.kalbskinder.patientZero.systems.scoreboard.ScoreboardUpdater;
import net.kalbskinder.patientZero.utils.ItemActionHandler;
import net.kalbskinder.patientZero.utils.PlayerCheck;
import net.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import net.kalbskinder.patientZero.commands.BaseCommand;

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
        logger.info("       Version: " + getPluginMeta().getVersion());
        logger.info("       Author: " + getPluginMeta().getAuthors().getFirst());
        logger.info("--------------------------------");
    }

    private void registerCommands() {
        new BaseCommand(this).register();
    }

    @Override
    public void onEnable() {
        startUpMessage();

        // Plugin startup logic
        saveDefaultConfig(); // Save default config file (config.yml)
        registerCommands(); // Register the '/ptz' command tree

        registerListeners();  // Register event listeners
        registerMethods(); // Register methods
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

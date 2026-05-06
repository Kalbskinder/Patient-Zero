package net.kalbskinder.patientZero;

import net.kalbskinder.patientZero.commands.*;
import net.kalbskinder.patientZero.listeners.*;
import net.kalbskinder.patientZero.systems.*;
import net.kalbskinder.patientZero.systems.scoreboard.ScoreboardSessionManager;
import net.kalbskinder.patientZero.systems.scoreboard.ScoreboardUpdater;
import net.kalbskinder.patientZero.utils.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class PatientZero extends JavaPlugin {
    private final PlayerCheck playerCheck = new PlayerCheck(getConfig());
    private final ItemMaker itemMaker = new ItemMaker();
    private final ItemActionHandler itemActionHandler = new ItemActionHandler(itemMaker);
    private final ItemDistributor itemDistributor = new ItemDistributor(this, itemMaker);

    private final QueueManager queueManager = new QueueManager(this, itemActionHandler, itemMaker, itemDistributor);
    private final TeleportPlayers teleportPlayers = queueManager.getTeleportPlayers();
    private final RoleUtils roleUtils = queueManager.getRoleUtils();
    private final ScoreboardUpdater scoreboardUpdater = queueManager.getScoreboardUpdater();
    private final ScoreboardSessionManager scoreboardSessionManager = queueManager.getScoreboardSessionManager();

    private final Queue queue = new Queue(queueManager);

    // Register event listeners
    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerQuit(queueManager), this); // Player quit event
        pm.registerEvents(new PlayerMove(queueManager, playerCheck, teleportPlayers), this); // Player move event
        pm.registerEvents(new ItemActionHandler(itemMaker), this); // Item right-click event
        pm.registerEvents(new PlayerDropItem(queueManager), this); // Drop item event
        pm.registerEvents(new PlayerChangeWorld(queueManager), this); // Player change world event
        pm.registerEvents(new PlayerTakeDamage(queueManager, scoreboardSessionManager, itemDistributor, teleportPlayers, roleUtils), this); // Player takes damage, handles respawn and game end mechanics
        pm.registerEvents(new EntityShootArrow(queueManager), this); // Entity shoot arrow event, makes it so players can't pick up the arrow
        pm.registerEvents(new PlayerBreakBlock(queueManager), this); // Player break block event, cancel event if player is queued
        pm.registerEvents(new DoubleJumpListener(this, queueManager), this); // Listener when player trys double jumping
    }

    // Pass the plugin instance to the methods
    private void registerMethods() {
        Prefixes.register(this);
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
        new BaseCommand(
                new CreateMapCommand(),
                new DeleteMapCommand(),
                new JoinLeaveCommand(queue, queueManager),
                new ListCommands(),
                new MapSpawnsCommand(),
                this,
                queue,
                new LocationSelection()
        );
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

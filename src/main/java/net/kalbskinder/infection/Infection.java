package net.kalbskinder.infection;

import net.kalbskinder.helpers.Helpers;
import net.kalbskinder.helpers.commands.CommandManager;
import net.kalbskinder.infection.commands.*;
import net.kalbskinder.infection.listeners.*;
import net.kalbskinder.infection.systems.*;
import net.kalbskinder.infection.systems.scoreboard.ScoreboardSessionManager;
import net.kalbskinder.infection.utils.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Infection extends JavaPlugin {
    private PlayerCheck playerCheck;
    private ItemMaker itemMaker;
    private ItemActionHandler itemActionHandler;
    private ItemDistributor itemDistributor;

    private QueueManager queueManager;
    private TeleportPlayers teleportPlayers;
    private RoleUtils roleUtils;
    private ScoreboardSessionManager scoreboardSessionManager;
    private LocationSelection locationSelection;

    private Queue queue;

    // Register event listeners
    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerQuit(queueManager, locationSelection), this); // Player quit event
        pm.registerEvents(new PlayerMove(queueManager, playerCheck, teleportPlayers), this); // Player move event
        pm.registerEvents(itemActionHandler, this); // Item right-click event
        pm.registerEvents(new PlayerDropItem(queueManager), this); // Drop item event
        pm.registerEvents(new PlayerChangeWorld(queueManager, locationSelection), this); // Player change world event
        pm.registerEvents(new PlayerTakeDamage(getConfig(), this, queueManager, scoreboardSessionManager, itemDistributor, teleportPlayers, roleUtils), this); // Player takes damage, handles respawn and game end mechanics
        pm.registerEvents(new EntityShootArrow(queueManager), this); // Entity shoot arrow event, makes it so players can't pick up the arrow
        pm.registerEvents(new PlayerBreakBlock(queueManager), this); // Player break block event, cancel event if player is queued
        pm.registerEvents(new PlayerInteract(this, locationSelection), this); // Player interact event, used for map creation (selection wand)
        pm.registerEvents(new PlayerJoin(this), this);
    }

    private void startUpMessage() {
        Logger logger = Logger.getLogger("Infection");
        logger.info("-------------------------------");
        logger.info("            Infection       ");
        logger.info("         Version: " + getPluginMeta().getVersion());
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
                new CommandManager(getLifecycleManager()),
                this,
                queue,
                locationSelection
        ).register();
    }

    @Override
    public void onEnable() {
        startUpMessage();

        // Plugin startup logic
        saveDefaultConfig(); // Save default config file (config.yml)

        Helpers.initialize(this);

        Prefixes.register(this);
        playerCheck = new PlayerCheck(getConfig());
        itemMaker = new ItemMaker(this);
        itemActionHandler = new ItemActionHandler(itemMaker);
        itemDistributor = new ItemDistributor(this, itemMaker);
        locationSelection = new LocationSelection(this);

        queueManager = new QueueManager(this, itemActionHandler, itemMaker, itemDistributor);
        teleportPlayers = queueManager.getTeleportPlayers();
        roleUtils = queueManager.getRoleUtils();
        scoreboardSessionManager = queueManager.getScoreboardSessionManager();
        queue = new Queue(queueManager);

        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

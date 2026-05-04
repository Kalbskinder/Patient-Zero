package net.kalbskinder.patientZero.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kalbskinder.patientZero.PatientZero;
import net.kalbskinder.patientZero.systems.Queue;
import net.kalbskinder.patientZero.utils.MMUtils;
import net.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class BaseCommand {
    private final PatientZero plugin;
    private static final String PREFIX = Prefixes.getPrefix();
    private static final List<String> SUBCOMMANDS = List.of(
            "help",
            "createmap",
            "listmaps",
            "deletemap",
            "addspawn",
            "setqueue-spawn",
            "setqueue-limit",
            "join",
            "leave"
    );

    public BaseCommand(PatientZero plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("ptz")
                    .executes(this::executeBase)
                    .then(Commands.literal("help")
                            .executes(this::executeHelp))
                    .then(Commands.literal("createmap")
                            .then(Commands.argument("map-name", StringArgumentType.word())
                                    .then(Commands.argument("x1", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("y1", DoubleArgumentType.doubleArg())
                                                    .then(Commands.argument("z1", DoubleArgumentType.doubleArg())
                                                            .then(Commands.argument("x2", DoubleArgumentType.doubleArg())
                                                                    .then(Commands.argument("y2", DoubleArgumentType.doubleArg())
                                                                            .then(Commands.argument("z2", DoubleArgumentType.doubleArg())
                                                                                    .executes(this::executeCreateMap)))))))))
                    .then(Commands.literal("listmaps")
                            .executes(this::executeListMaps))
                    .then(Commands.literal("deletemap")
                            .then(Commands.argument("map-name", StringArgumentType.word())
                                    .suggests(this::suggestMapNames)
                                    .executes(this::executeDeleteMap)))
                    .then(Commands.literal("addspawn")
                            .then(Commands.argument("map-name", StringArgumentType.word())
                                    .suggests(this::suggestMapNames)
                                    .then(Commands.argument("role", StringArgumentType.word())
                                            .suggests((ignoredContext, builder) -> {
                                                builder.suggest("corrupted");
                                                builder.suggest("survivor");
                                                return builder.buildFuture();
                                            })
                                            .executes(this::executeAddSpawn))))
                    .then(Commands.literal("setqueue-spawn")
                            .then(Commands.argument("map-name", StringArgumentType.word())
                                    .suggests(this::suggestMapNames)
                                    .executes(this::executeSetQueueSpawn)))
                    .then(Commands.literal("setqueue-limit")
                            .then(Commands.argument("map-name", StringArgumentType.word())
                                    .suggests(this::suggestMapNames)
                                    .then(Commands.argument("int-limit", IntegerArgumentType.integer(1))
                                            .executes(this::executeSetQueueLimit))))
                    .then(Commands.literal("join")
                            .then(Commands.argument("map-name", StringArgumentType.word())
                                    .suggests(this::suggestMapNames)
                                    .executes(this::executeJoin)))
                    .then(Commands.literal("leave")
                            .executes(this::executeLeave))
                    .then(Commands.argument("subcommand", StringArgumentType.greedyString())
                            .suggests(this::suggestSubcommands)
                            .executes(this::executeUnknownSubcommand));

            event.registrar().register(root.build(), "Main command", List.of("patient-zero"));
        });
    }

    private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestMapNames(CommandContext<CommandSourceStack> ignoredContext, com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        ConfigurationSection maps = plugin.getConfig().getConfigurationSection("maps");
        if (maps == null) {
            return builder.buildFuture();
        }

        for (String mapName : maps.getKeys(false)) {
            builder.suggest(mapName);
        }
        return builder.buildFuture();
    }

    private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestSubcommands(CommandContext<CommandSourceStack> context, com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        CommandSender sender = context.getSource().getSender();
        Player player = sender instanceof Player p ? p : null;

        boolean canAdmin = player == null || player.hasPermission("ptz.admin");
        boolean canJoin = player == null || player.hasPermission("ptz.join");
        boolean canLeave = player == null || player.hasPermission("ptz.leave");

        for (String subcommand : SUBCOMMANDS) {
            if ((subcommand.equals("join") && !canJoin)
                    || (subcommand.equals("leave") && !canLeave)
                    || (!subcommand.equals("join") && !subcommand.equals("leave") && !canAdmin)) {
                continue;
            }
            builder.suggest(subcommand);
        }

        return builder.buildFuture();
    }

    private Player requirePlayer(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (sender instanceof Player player) {
            return player;
        }

        sender.sendMessage("Only players can execute this command.");
        return null;
    }

    private int executeBase(CommandContext<CommandSourceStack> context) {
        Player player = requirePlayer(context);
        if (player == null || !playerHasPermission("ptz.admin", player)) {
            return Command.SINGLE_SUCCESS;
        }

        MMUtils.sendMessage(player, PREFIX + "<gray>Use <yellow>/ptz help <gray>for a list of commands.<reset>");
        return Command.SINGLE_SUCCESS;
    }

    private int executeHelp(CommandContext<CommandSourceStack> context) {
        Player player = requirePlayer(context);
        if (player == null || !playerHasPermission("ptz.admin", player)) {
            return Command.SINGLE_SUCCESS;
        }

        MMUtils.sendMessage(player, PREFIX + "<green>Available commands:<reset>");
        MMUtils.sendMessage(player, "<gray>- <yellow>/ptz <gold>createmap <gray><map-name> <x1> <y1> <z1> <x2> <y2> <z2>");
        MMUtils.sendMessage(player, "<gray>- <yellow>/ptz <gold>deletemap <gray><map-name>");
        MMUtils.sendMessage(player, "<gray>- <yellow>/ptz <gold>addspawn <gray><map-name> <role>");
        MMUtils.sendMessage(player, "<gray>- <yellow>/ptz <gold>setqueue-spawn <gray><map-name>");
        MMUtils.sendMessage(player, "<gray>- <yellow>/ptz <gold>setqueue-limit <gray><map-name> <int-limit>");
        MMUtils.sendMessage(player, "<gray>- <yellow>/ptz <gold>listmaps");
        MMUtils.sendMessage(player, "");
        MMUtils.sendMessage(player, "<gray>- <yellow>/ptz <gold>join <gray><map-name>");
        MMUtils.sendMessage(player, "<gray>- <yellow>/ptz <gold>leave");
        return Command.SINGLE_SUCCESS;
    }

    private int executeCreateMap(CommandContext<CommandSourceStack> context) {
        Player player = requirePlayer(context);
        if (player == null || !playerHasPermission("ptz.admin", player)) {
            return Command.SINGLE_SUCCESS;
        }

        String[] args = {
                "createmap",
                StringArgumentType.getString(context, "map-name"),
                String.valueOf(DoubleArgumentType.getDouble(context, "x1")),
                String.valueOf(DoubleArgumentType.getDouble(context, "y1")),
                String.valueOf(DoubleArgumentType.getDouble(context, "z1")),
                String.valueOf(DoubleArgumentType.getDouble(context, "x2")),
                String.valueOf(DoubleArgumentType.getDouble(context, "y2")),
                String.valueOf(DoubleArgumentType.getDouble(context, "z2"))
        };

        CreateMapCommand.createMap(context.getSource().getSender(), args, player, plugin);
        return Command.SINGLE_SUCCESS;
    }

    private int executeListMaps(CommandContext<CommandSourceStack> context) {
        Player player = requirePlayer(context);
        if (player == null || !playerHasPermission("ptz.admin", player)) {
            return Command.SINGLE_SUCCESS;
        }

        ListCommands.listMaps(context.getSource().getSender(), new String[]{"listmaps"}, player, plugin);
        return Command.SINGLE_SUCCESS;
    }

    private int executeDeleteMap(CommandContext<CommandSourceStack> context) {
        Player player = requirePlayer(context);
        if (player == null || !playerHasPermission("ptz.admin", player)) {
            return Command.SINGLE_SUCCESS;
        }

        String[] args = {"deletemap", StringArgumentType.getString(context, "map-name")};
        DeleteMapCommand.deleteMap(context.getSource().getSender(), args, player, plugin);
        return Command.SINGLE_SUCCESS;
    }

    private int executeAddSpawn(CommandContext<CommandSourceStack> context) {
        Player player = requirePlayer(context);
        if (player == null || !playerHasPermission("ptz.admin", player)) {
            return Command.SINGLE_SUCCESS;
        }

        String role = StringArgumentType.getString(context, "role");
        if (!role.equalsIgnoreCase("corrupted") && !role.equalsIgnoreCase("survivor")) {
            MMUtils.sendMessage(player, PREFIX + "<red><role> has to be \"corrupted\" or \"survivor\".");
            return Command.SINGLE_SUCCESS;
        }

        String[] args = {"addspawn", StringArgumentType.getString(context, "map-name"), role};
        MapSpawnsCommand.addMapSpawns(context.getSource().getSender(), args, player, plugin);
        return Command.SINGLE_SUCCESS;
    }

    private int executeSetQueueSpawn(CommandContext<CommandSourceStack> context) {
        Player player = requirePlayer(context);
        if (player == null || !playerHasPermission("ptz.admin", player)) {
            return Command.SINGLE_SUCCESS;
        }

        String[] args = {"setqueue-spawn", StringArgumentType.getString(context, "map-name")};
        MapSpawnsCommand.setQueueSpawn(context.getSource().getSender(), args, player, plugin);
        return Command.SINGLE_SUCCESS;
    }

    private int executeSetQueueLimit(CommandContext<CommandSourceStack> context) {
        Player player = requirePlayer(context);
        if (player == null || !playerHasPermission("ptz.admin", player)) {
            return Command.SINGLE_SUCCESS;
        }

        String[] args = {
                "setqueue-limit",
                StringArgumentType.getString(context, "map-name"),
                String.valueOf(IntegerArgumentType.getInteger(context, "int-limit"))
        };
        Queue.setQueueLimit(context.getSource().getSender(), args, player, plugin);
        return Command.SINGLE_SUCCESS;
    }

    private int executeJoin(CommandContext<CommandSourceStack> context) {
        Player player = requirePlayer(context);
        if (player == null || !playerHasPermission("ptz.join", player)) {
            return Command.SINGLE_SUCCESS;
        }

        String[] args = {"join", StringArgumentType.getString(context, "map-name")};
        JoinLeaveCommand.joinMap(args, player, plugin);
        return Command.SINGLE_SUCCESS;
    }

    private int executeLeave(CommandContext<CommandSourceStack> context) {
        Player player = requirePlayer(context);
        if (player == null || !playerHasPermission("ptz.leave", player)) {
            return Command.SINGLE_SUCCESS;
        }

        JoinLeaveCommand.leaveMap(player, plugin);
        return Command.SINGLE_SUCCESS;
    }

    private int executeUnknownSubcommand(CommandContext<CommandSourceStack> context) {
        Player player = requirePlayer(context);
        if (player == null) {
            return Command.SINGLE_SUCCESS;
        }

        MMUtils.sendMessage(player, PREFIX + "<red>Unknown subcommand. Use <yellow>/ptz help <red>for a list of commands.");
        return Command.SINGLE_SUCCESS;
    }

    // Checks if a player has the required permission or is an admin.
    // Sends an error message if not.
    private boolean playerHasPermission(String permission, Player player) {
        if (player.hasPermission(permission)) {
            return true;
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
            MMUtils.sendMessage(player, PREFIX + "<red>You don't have permission to use this command!");
            return false;
        }
    }
}
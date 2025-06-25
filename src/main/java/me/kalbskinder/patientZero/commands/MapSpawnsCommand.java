package me.kalbskinder.patientZero.commands;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSpawnsCommand {
    private static String prefix = Prefixes.getPrefix();

    public static void addMapSpawns(CommandSender sender, String[] args, Player player, PatientZero plugin) {
        String mapName = args[1];
        String role = args[2].toLowerCase(); // "corrupted" or "survivor"

        Location location = player.getLocation();
        FileConfiguration config = plugin.getConfig();

        String path = "maps." + mapName + ".spawns." + role;

        List<Map<?, ?>> spawns = config.getMapList(path);

        Map<String, Object> newSpawn = new HashMap<>();
        newSpawn.put("world", location.getWorld().getName());
        newSpawn.put("x", location.getX());
        newSpawn.put("y", location.getY());
        newSpawn.put("z", location.getZ());
        newSpawn.put("yaw", (double) location.getYaw());
        newSpawn.put("pitch", (double) location.getPitch());

        spawns.add(newSpawn);

        config.set(path, spawns);
        plugin.saveConfig();

        // Send a chat message to notify the player that the spawnpoint was created succesfully
        player.sendMessage(prefix + "§aAdded Spawnpoint for " + role + " at:");
        player.sendMessage(location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }

    public static void setQueueSpawn(CommandSender sender, String[] args, Player player, PatientZero plugin) {
        String mapName = args[1];
        FileConfiguration config = plugin.getConfig();

        // Check if map exists
        if (!config.contains("maps." + mapName)) {
            sender.sendMessage(prefix + "§cMap not found!");
            return;
        }

        Location loc = player.getLocation();

        // Extract player location data
        List<Object> serializedLocation = Arrays.asList(
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        );

        // Write queue-spawn to config
        config.set("maps." + mapName + ".spawns.queue-spawn", serializedLocation);
        plugin.saveConfig();

        sender.sendMessage(prefix + "§aQueue-Spawn for map §e" + mapName + " §ahas been created.");
        sender.sendMessage(prefix + "If you get an §cerror §rwhen executing §a'/ptz join " + mapName + "'§r, please §creastart your server§r and try again.");
    }

}
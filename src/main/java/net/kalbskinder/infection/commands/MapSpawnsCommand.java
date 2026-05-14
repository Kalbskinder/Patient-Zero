package net.kalbskinder.infection.commands;

import net.kalbskinder.infection.Infection;
import net.kalbskinder.infection.utils.MMUtils;
import net.kalbskinder.infection.utils.Prefixes;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSpawnsCommand {
    private static final String PREFIX = Prefixes.getPrefix();

    public void addMapSpawns(CommandSender sender, String mapName, String role, Player player, Infection plugin) {

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
        MMUtils.sendMessage(player, PREFIX + "<green>Added Spawnpoint for " + role + " at:");
        MMUtils.sendMessage(player,location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }

    public void setQueueSpawn(CommandSender sender, String mapName, Player player, Infection plugin) {
        FileConfiguration config = plugin.getConfig();

        // Check if map exists
        if (!config.contains("maps." + mapName)) {
            MMUtils.sendMessage(player, PREFIX + "<red>Map not found!");
            return;
        }

        Location loc = player.getLocation();

        // Extract player location data
        List<Object> serializedLocation = Arrays.asList(
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                (double) loc.getYaw(),
                (double) loc.getPitch()
        );

        // Write queue-spawn to config
        config.set("maps." + mapName + ".spawns.queue-spawn", serializedLocation);
        plugin.saveConfig();

        MMUtils.sendMessage(player, PREFIX + "<green>Added Spawnpoint for " + mapName + " at:");
        MMUtils.sendMessage(player, PREFIX + "<green>Queue spawn saved. Players can now join this map.");
    }

}
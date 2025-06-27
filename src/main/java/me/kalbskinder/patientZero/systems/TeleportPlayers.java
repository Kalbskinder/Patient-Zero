package me.kalbskinder.patientZero.systems;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.utils.MMUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TeleportPlayers {
    private static FileConfiguration config;
    private static Logger logger = Logger.getLogger("PTZ");

    // Get config from plugin instance
    public static void register(PatientZero plugin) {
        config = plugin.getConfig();
    }

    // Reads the possible random locations from the config file
    public static void teleportPlayersOnGameStart(QueueInfo queue) {
        if (queue == null || queue.getPlayers().isEmpty()) {
            logger.warning("Queue is null or has no players");
            return;
        }

        String map = QueueManager.getMapOfPlayer(queue.getPlayers().get(0)); // Get the map name by the first player
        if (map == null) {
            logger.warning("Map name is null for player: " + queue.getPlayers().get(0).getName());
            return;
        }

        List<Location> locations = new ArrayList<>();
        String basePath = "maps." + map + ".spawns.";

        // Load survivor and corrupted spawns as single list
        locations.addAll(loadSpawnLocations(basePath + "survivor", "survivor"));
        locations.addAll(loadSpawnLocations(basePath + "corrupted", "corrupted"));

        // Check if there are spawn locations configured for the map
        // This can be done by using the in game command '/ptz addspawn <map-name> <role>'
        if (locations.isEmpty()) {
            logger.warning("No spawn locations available for map: " + map);
            queue.getPlayers().forEach(player -> { MMUtils.sendMessage(player, "<red>No spawn locations found! You can configure them by using '/ptz addspawn <map-name> <role>'. For more information type '/ptz help'"); });
            return;
        }

        List<Player> players = queue.getPlayers();
        teleportPlayersToRandomLocations(players, locations);
    }

    // Read each possible location and map it to a new list
    private static List<Location> loadSpawnLocations(String path, String sectionName) {
        List<Location> locations = new ArrayList<>();
        List<Map<?, ?>> spawnList = config.getMapList(path);

        // Make sure that the config returned valid sections
        if (spawnList == null || spawnList.isEmpty()) {
            logger.warning("Spawn list is null or empty for " + sectionName + " at path: " + path);
            return locations;
        }

        for (int i = 0; i < spawnList.size(); i++) {
            Map<?, ?> spawn = spawnList.get(i);
            String worldName = (String) spawn.get("world");
            if (worldName == null) {
                logger.warning("World name is null for spawn entry " + i + " in " + sectionName);
                continue;
            }

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                logger.warning("World not found: " + worldName);
                continue;
            }

            try {
                double x = ((Number) spawn.get("x")).doubleValue();
                double y = ((Number) spawn.get("y")).doubleValue();
                double z = ((Number) spawn.get("z")).doubleValue();
                float yaw = spawn.containsKey("yaw") ? ((Number) spawn.get("yaw")).floatValue() : 0f;
                float pitch = spawn.containsKey("pitch") ? ((Number) spawn.get("pitch")).floatValue() : 0f;

                Location loc = new Location(world, x, y, z, yaw, pitch);
                locations.add(loc);
            } catch (Exception e) {
                logger.warning("Invalid spawn format at index " + i + ": " + e.getMessage());
            }
        }

        return locations;
    }


    // Teleport each player to a random location
    public static void teleportPlayersToRandomLocations(List<Player> players, List<Location> locations) {
        if (players == null || locations == null || players.isEmpty() || locations.isEmpty()) {
            logger.warning("Players or locations list is empty or null");
            return;
        }

        // Teleport each player to a random location that was configured
        for (Player player : players) {
            // Skip offline players
            if (!player.isOnline()) {
                logger.warning("Player " + player.getName() + " is offline, skipping teleport");
                continue;
            }

            // Get random location
            int randomIndex = (int) (Math.random() * locations.size());
            Location targetLocation = locations.get(randomIndex);
            if (!player.teleport(targetLocation)) {
                logger.warning("Failed to teleport player " + player.getName() + " to: " + targetLocation);
                MMUtils.sendMessage(player, "<red>Teleportation failed! Please contact a staff member!");
            }
        }
    }

    // Teleports a player to a random defined corrupted spawn location
    public static void teleportPlayerToCorruptedLocations(Player player) {
        List<Location> locations = new ArrayList<>();
        List<Player> players = new ArrayList<>();
        players.add(player);

        String mapName = QueueManager.getMapOfPlayer(player);

        // Get the spawnpoints
        locations.addAll(loadSpawnLocations("maps." + mapName + ".spawns.corrupted", "corrupted"));

        if (locations.isEmpty()) {
            logger.warning("No spawn locations available for map: " + mapName);
            return;
        }

        // Teleport the player
        teleportPlayersToRandomLocations(players, locations);
    }
}
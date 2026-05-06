package net.kalbskinder.patientZero.systems;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.patientZero.utils.MMUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class TeleportPlayers {
    private final FileConfiguration config;
    private final QueueManager queueManager;

    private static final Logger LOGGER = Logger.getLogger("PTZ");

    // Reads the possible random locations from the config file
    public void teleportPlayersOnGameStart(QueueInfo queue) {
        if (queue == null || queue.getPlayers().isEmpty()) {
            LOGGER.warning("Queue is null or has no players");
            return;
        }

        String map = queueManager.getMapOfPlayer(queue.getPlayers().getFirst()); // Get the map name by the first player
        if (map == null) {
            LOGGER.warning("Map name is null for player: " + queue.getPlayers().getFirst().getName());
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
            LOGGER.warning("No spawn locations available for map: " + map);
            queue.getPlayers().forEach(player -> { MMUtils.sendMessage(player, "<red>No spawn locations found! You can configure them by using '/ptz addspawn <map-name> <role>'. For more information type '/ptz help'"); });
            return;
        }

        List<Player> players = queue.getPlayers();
        teleportPlayersToRandomLocations(players, locations);
    }

    // Read each possible location and map it to a new list
    private List<Location> loadSpawnLocations(String path, String sectionName) {
        List<Location> locations = new ArrayList<>();
        List<Map<?, ?>> spawnList = config.getMapList(path);

        // Make sure that the config returned valid sections
        if (spawnList.isEmpty()) {
            LOGGER.warning("Spawn list is null or empty for " + sectionName + " at path: " + path);
            return locations;
        }

        for (int i = 0; i < spawnList.size(); i++) {
            Map<?, ?> spawn = spawnList.get(i);
            String worldName = (String) spawn.get("world");
            if (worldName == null) {
                LOGGER.warning("World name is null for spawn entry " + i + " in " + sectionName);
                continue;
            }

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                LOGGER.warning("World not found: " + worldName);
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
                LOGGER.warning("Invalid spawn format at index " + i + ": " + e.getMessage());
            }
        }

        return locations;
    }


    // Teleport each player to a random location
    public void teleportPlayersToRandomLocations(List<Player> players, List<Location> locations) {
        if (players == null || locations == null || players.isEmpty() || locations.isEmpty()) {
            LOGGER.warning("Players or locations list is empty or null");
            return;
        }

        // Teleport each player to a random location that was configured
        for (Player player : players) {
            // Skip offline players
            if (!player.isOnline()) {
                LOGGER.warning("Player " + player.getName() + " is offline, skipping teleport");
                continue;
            }

            // Get random location
            int randomIndex = (int) (Math.random() * locations.size());
            Location targetLocation = locations.get(randomIndex);
            if (!player.teleport(targetLocation)) {
                LOGGER.warning("Failed to teleport player " + player.getName() + " to: " + targetLocation);
                MMUtils.sendMessage(player, "<red>Teleportation failed! Please contact a staff member!");
            }
        }
    }

    // Teleports a player to a random defined corrupted spawn location
    public void teleportPlayerToCorruptedLocations(Player player) {
        List<Player> players = new ArrayList<>();
        players.add(player);

        String mapName = queueManager.getMapOfPlayer(player);

        // Get the spawnpoints
        List<Location> locations = new ArrayList<>(loadSpawnLocations("maps." + mapName + ".spawns.corrupted", "corrupted"));

        if (locations.isEmpty()) {
            LOGGER.warning("No spawn locations available for map: " + mapName);
            return;
        }

        // Teleport the player
        teleportPlayersToRandomLocations(players, locations);
    }
}
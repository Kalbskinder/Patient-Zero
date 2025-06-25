package me.kalbskinder.patientZero.utils;

import me.kalbskinder.patientZero.PatientZero;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PlayerCheck {
    private static FileConfiguration config;

    public static void register(PatientZero plugin) {
        config = plugin.getConfig();
    }

    /**
     * Checks whether the given location is inside the rectangular area defined by two corner positions.
     *
     * @param loc The location of the player to check for
     * @param pos1 The position of the first corner of the area to check for
     * @param pos2 The position of the second corner (diagonal of the first) of the area to check for
     * @return Returns true if the player is inside the specified area, false otherwise
     */
    public static boolean isInsideArea(Location loc, Location pos1, Location pos2) {
        if (loc == null || pos1 == null || pos2 == null) return false;
        if (!loc.getWorld().equals(pos1.getWorld())) return false;

        // Get min/max values of the boundaries
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        // Parse players location
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        // Check if the player is inside the area
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    // Reads the configured boundaries of a map from the config
    public static ArrayList<Location> getMapArea (String mapName, World world) {
        List<?> pos1 = config.getList("maps." + mapName + ".area.pos1");
        List<?> pos2 = config.getList("maps." + mapName + ".area.pos2");

        // Build the positions into valid locations
        ArrayList<Location> locations = new ArrayList<>();
        locations.add(buildLocation(pos1, world));
        locations.add(buildLocation(pos2, world));

        return locations;
    }

    // Reads the x, y, z values from a list. Builds a location and returns it.
    public static Location buildLocation(List<?> data, World world) {
        if (data == null || data.size() < 3) return null;

        try {
            double x = ((Number) data.get(0)).doubleValue();
            double y = ((Number) data.get(1)).doubleValue();
            double z = ((Number) data.get(2)).doubleValue();

            return new Location(world, x, y, z);
        } catch (Exception e) {
            Logger.getLogger("PTZ").warning("Failed to parse map coordinates.");
            return null;
        }
    }
}

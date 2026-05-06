package net.kalbskinder.patientZero.commands;

import net.kalbskinder.patientZero.PatientZero;
import net.kalbskinder.patientZero.utils.MMUtils;
import net.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CreateMapCommand {
    private static final String PREFIX = Prefixes.getPrefix();

    public void createMap(String mapName, Location loc1, Location loc2, Player player, PatientZero plugin) {
        FileConfiguration config = plugin.getConfig();

        // Check if a map with this name already exists
        if (config.contains("maps." + mapName)) {
            MMUtils.sendMessage(player, PREFIX + "<red>A map with this name already exists!");
            return;
        }

        try {
            // Parse coordinates
            double x1 = loc1.getX();
            double y1 = loc1.getY();
            double z1 = loc1.getZ();
            double x2 = loc2.getX();
            double y2 = loc2.getY();
            double z2 = loc2.getZ();

            String basePath = "maps." + mapName;

            // Save map data
            config.set(basePath + ".name", mapName);
            config.set(basePath + ".area.pos1", Arrays.asList(x1, y1, z1));
            config.set(basePath + ".area.pos2", Arrays.asList(x2, y2, z2));

            // Initialize empty spawn lists
            config.set(basePath + ".spawns.survivor", null);
            config.set(basePath + ".spawns.corrupted", null);

            plugin.saveConfig();

            // Send a confirmation message
            MMUtils.sendMessage(player, PREFIX + "<green>Map '" + mapName + "' has been created successfully!");
        } catch (NumberFormatException e) {
            MMUtils.sendMessage(player, PREFIX + "<red>Failed to parse coordinates.");
        }
    }
}

package me.kalbskinder.patientZero.commands;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.utils.MMUtils;
import me.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CreateMapCommand {
    public static void createMap(CommandSender sender, String[] args, Player player, PatientZero plugin) {
        String prefix = Prefixes.getPrefix();
        String mapName = args[1];
        FileConfiguration config = plugin.getConfig();

        // Check if a map with this name already exists
        if (config.contains("maps." + mapName)) {
            MMUtils.sendMessage(player, prefix + "<red>A map with this name already exists!");
            return;
        }

        try {
            // Parse coordinates
            double x1 = Double.parseDouble(args[2]);
            double y1 = Double.parseDouble(args[3]);
            double z1 = Double.parseDouble(args[4]);
            double x2 = Double.parseDouble(args[5]);
            double y2 = Double.parseDouble(args[6]);
            double z2 = Double.parseDouble(args[7]);

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
            MMUtils.sendMessage(player, prefix + "<green>Map '" + mapName + "' has been created successfully!");

        } catch (NumberFormatException e) {
            MMUtils.sendMessage(player, prefix + "<red>Coordinates must be valid numbers.");
        }
    }
}

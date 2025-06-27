package me.kalbskinder.patientZero.commands;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.utils.MMUtils;
import me.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class DeleteMapCommand {
    public static void deleteMap(CommandSender sender, String[] args, Player player, PatientZero plugin) {
        String mapName = args[1]; // The map to delete
        String prefix = Prefixes.getPrefix();

        FileConfiguration config = plugin.getConfig();
        String path = "maps." + mapName;

        // Check if the map exists
        if (config.contains(path)) {
            config.set(path, null); // Delete the map section
            plugin.saveConfig();
            MMUtils.sendMessage(player, prefix + "<green>Map <yellow>" + mapName + " <green>has been deleted.");
        } else {
            MMUtils.sendMessage(player, prefix + "<red>Map <yellow>" + mapName + " <red>was not found.");
        }
    }
}

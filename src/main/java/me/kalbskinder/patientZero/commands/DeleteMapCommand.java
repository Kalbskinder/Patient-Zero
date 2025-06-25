package me.kalbskinder.patientZero.commands;

import me.kalbskinder.patientZero.PatientZero;
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
            sender.sendMessage(prefix + "§aMap §e" + mapName + " §ahas been deleted.");
        } else {
            sender.sendMessage(prefix + "§cMap §e" + mapName + " §cwas not found.");
        }
    }
}

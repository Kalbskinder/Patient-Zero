package me.kalbskinder.patientZero.commands;

import me.kalbskinder.patientZero.PatientZero;
import me.kalbskinder.patientZero.utils.MMUtils;
import me.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Set;

public class ListCommands {
    public static void listMaps(CommandSender sender, String[] args, Player player, PatientZero plugin) {
        String prefix = Prefixes.getPrefix();
        FileConfiguration config = plugin.getConfig();

        // Get all keys under 'maps' (map names)
        if (!config.isConfigurationSection("maps")) {
            MMUtils.sendMessage(player, prefix + "<red>No maps saved!");
            return;
        }

        Set<String> mapNames = config.getConfigurationSection("maps").getKeys(false);

        if (mapNames.isEmpty()) {
            MMUtils.sendMessage(player, prefix + "<red>No maps saved!");
            return;
        }

        MMUtils.sendMessage(player, prefix + "<green>Saved maps:");
        for (String map : mapNames) {
            MMUtils.sendMessage(player, "<gray>- <white>" + map);
        }
    }
}

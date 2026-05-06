package net.kalbskinder.patientZero.commands;

import net.kalbskinder.patientZero.PatientZero;
import net.kalbskinder.patientZero.utils.MMUtils;
import net.kalbskinder.patientZero.utils.Prefixes;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Set;

public class ListCommands {
    private static final String PREFIX = Prefixes.getPrefix();
    
    public void listMaps(CommandSender sender, String[] args, Player player, PatientZero plugin) {
        FileConfiguration config = plugin.getConfig();

        // Get all keys under 'maps' (map names)
        if (!config.isConfigurationSection("maps")) {
            MMUtils.sendMessage(player, PREFIX + "<red>No maps saved!");
            return;
        }

        Set<String> mapNames = config.getConfigurationSection("maps").getKeys(false);

        if (mapNames.isEmpty()) {
            MMUtils.sendMessage(player, PREFIX + "<red>No maps saved!");
            return;
        }

        MMUtils.sendMessage(player, PREFIX + "<green>Saved maps:");
        for (String map : mapNames) {
            MMUtils.sendMessage(player, "<gray>- <white>" + map);
        }
    }
}

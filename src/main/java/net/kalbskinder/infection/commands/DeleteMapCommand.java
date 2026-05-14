package net.kalbskinder.infection.commands;

import net.kalbskinder.infection.Infection;
import net.kalbskinder.infection.utils.MMUtils;
import net.kalbskinder.infection.utils.Prefixes;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class DeleteMapCommand {
    private static final String PREFIX = Prefixes.getPrefix();

    public void deleteMap(CommandSender sender, String mapName, Player player, Infection plugin) {
        FileConfiguration config = plugin.getConfig();
        String path = "maps." + mapName;

        // Check if the map exists
        if (config.contains(path)) {
            config.set(path, null); // Delete the map section
            plugin.saveConfig();
            MMUtils.sendMessage(player, PREFIX + "<green>Map <yellow>" + mapName + " <green>has been deleted.");
        } else {
            MMUtils.sendMessage(player, PREFIX + "<red>Map <yellow>" + mapName + " <red>was not found.");
        }
    }
}

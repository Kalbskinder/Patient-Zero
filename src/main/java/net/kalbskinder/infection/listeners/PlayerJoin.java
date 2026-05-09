package net.kalbskinder.infection.listeners;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.infection.Infection;
import net.kalbskinder.infection.utils.MMUtils;
import net.kalbskinder.infection.utils.Prefixes;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public class PlayerJoin implements Listener {
    private static final String PREFIX = Prefixes.getPrefix();
    private final Infection plugin;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("ptz.admin")) {
            FileConfiguration config = plugin.getConfig();

            // no maps exist yet
            if (!config.isConfigurationSection("maps")) {
                MMUtils.sendMessage(player, PREFIX + "<green>We've noticed you haven't created any maps yet.");
                MMUtils.sendMessage(player, PREFIX + "<green>Use <light_purple>/infection help guide <green>to get started!");
            }
        }
    }
}

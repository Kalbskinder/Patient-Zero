package net.kalbskinder.patientZero.utils;

import lombok.RequiredArgsConstructor;
import net.kalbskinder.patientZero.enums.PlayerRole;
import net.kalbskinder.patientZero.systems.QueueInfo;
import net.kalbskinder.patientZero.systems.QueueManager;
import org.bukkit.entity.Player;

import java.util.Map;

@RequiredArgsConstructor
public class RoleUtils {
    private final QueueManager queueManager;

    // Checks if a player with the given role is still alive
    public boolean isRoleAlive(String mapName, PlayerRole role) {
        QueueInfo queue = queueManager.getQueueInfo(mapName);

        if (queue == null || queue.getRoles() == null) return false;

        for (Map.Entry<Player, PlayerRole> entry : queue.getRoles().entrySet()) {
            Player player = entry.getKey();
            PlayerRole playerRole = entry.getValue();

            if (playerRole == role && player.isOnline() && !player.isDead()) {
                return true;
            }
        }
        return false;
    }
}

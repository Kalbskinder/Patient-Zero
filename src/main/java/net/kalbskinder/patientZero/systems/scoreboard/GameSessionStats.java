package net.kalbskinder.patientZero.systems.scoreboard;

import lombok.Getter;
import lombok.Setter;
import net.kalbskinder.patientZero.enums.PlayerRole;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class GameSessionStats {
    private int timer;
    private int survivorsCount;
    private int corruptedCount;
    private String mapName;
    private final Map<UUID, Integer> playerKills = new HashMap<>();
    private final Map<Player, PlayerRole> playerRoles = new HashMap<>();

    public void setPlayerRoles(Map<Player, PlayerRole> playerRoles) {
        this.playerRoles.clear();
        this.playerRoles.putAll(playerRoles);
    }
}

package net.kalbskinder.infection.systems;

import lombok.Getter;
import lombok.Setter;
import net.kalbskinder.infection.enums.GameState;
import net.kalbskinder.infection.enums.PlayerRole;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueueInfo {
    private List<Player> players = new ArrayList<>();
    private BukkitTask countdownTask;
    private boolean isCountingDown = false;
    private GameState state = GameState.WAITING;
    private PlayerRole gameWinners;
    private final Map<Player, PlayerRole> roles = new HashMap<>();
}

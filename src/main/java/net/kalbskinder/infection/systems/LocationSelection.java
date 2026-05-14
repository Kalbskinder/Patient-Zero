package net.kalbskinder.infection.systems;

import net.kalbskinder.infection.Infection;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LocationSelection {
    private static final Particle.DustOptions RED_DUST = new Particle.DustOptions(Color.RED, 1.35f);

    private final Infection plugin;
    private final Map<UUID, SelectionState> selections = new HashMap<>();

    public LocationSelection(Infection plugin) {
        this.plugin = plugin;
    }

    public Location getPos1(Player player) {
        SelectionState selection = selections.get(player.getUniqueId());
        return selection != null ? selection.getPos1() : null;
    }

    public Location getPos2(Player player) {
        SelectionState selection = selections.get(player.getUniqueId());
        return selection != null ? selection.getPos2() : null;
    }

    public void setPos1(Player player, Location location) {
        SelectionState selection = getOrCreateSelection(player);
        selection.setPos1(copyLocation(location));
        refreshOutline(player, selection);
    }

    public void setPos2(Player player, Location location) {
        SelectionState selection = getOrCreateSelection(player);
        selection.setPos2(copyLocation(location));
        refreshOutline(player, selection);
    }

    public void clearSelection(Player player) {
        SelectionState selection = selections.remove(player.getUniqueId());
        if (selection != null) {
            selection.cancelTask();
        }
    }

    private SelectionState getOrCreateSelection(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), ignored -> new SelectionState());
    }

    private void refreshOutline(Player player, SelectionState selection) {
        selection.cancelTask();
        renderSelection(player, selection);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !selections.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }

                renderSelection(player, selection);
            }
        }.runTaskTimer(plugin, 20L, 20L);

        selection.setTask(task);
    }

    private void renderSelection(Player player, SelectionState selection) {
        Location pos1 = selection.getPos1();
        Location pos2 = selection.getPos2();

        if (pos1 != null) {
            spawnPoint(player, pos1);
        }

        if (pos2 != null) {
            spawnPoint(player, pos2);
        }

        if (pos1 == null || pos2 == null) {
            return;
        }

        if (pos1.getWorld() == null || pos2.getWorld() == null || !pos1.getWorld().equals(pos2.getWorld())) {
            return;
        }

        spawnOutline(player, pos1, pos2);
    }

    private void spawnPoint(Player player, Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        Location center = toCenter(location);
        player.spawnParticle(Particle.DUST, center, 8, 0.05, 0.05, 0.05, 0.0, RED_DUST);
    }

    private void spawnOutline(Player player, Location pos1, Location pos2) {
        World world = pos1.getWorld();
        if (world == null) {
            return;
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        Set<String> points = new HashSet<>();

        for (int x = minX; x <= maxX; x++) {
            addPoint(points, x, minY, minZ);
            addPoint(points, x, minY, maxZ);
            addPoint(points, x, maxY, minZ);
            addPoint(points, x, maxY, maxZ);
        }

        for (int y = minY; y <= maxY; y++) {
            addPoint(points, minX, y, minZ);
            addPoint(points, minX, y, maxZ);
            addPoint(points, maxX, y, minZ);
            addPoint(points, maxX, y, maxZ);
        }

        for (int z = minZ; z <= maxZ; z++) {
            addPoint(points, minX, minY, z);
            addPoint(points, minX, maxY, z);
            addPoint(points, maxX, minY, z);
            addPoint(points, maxX, maxY, z);
        }

        for (String encoded : points) {
            String[] parts = encoded.split(":");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            player.spawnParticle(Particle.DUST, x + 0.5, y + 0.5, z + 0.5, 8, 0.05, 0.05, 0.05, 0.0, RED_DUST);
        }
    }

    private void addPoint(Set<String> points, int x, int y, int z) {
        points.add(x + ":" + y + ":" + z);
    }

    private Location copyLocation(Location location) {
        return location == null ? null : location.clone();
    }

    private Location toCenter(Location location) {
        return location.clone().add(0.5, 0.5, 0.5);
    }

    private static final class SelectionState {
        private Location pos1;
        private Location pos2;
        private BukkitTask task;

        public Location getPos1() {
            return pos1;
        }

        public void setPos1(Location pos1) {
            this.pos1 = pos1;
        }

        public Location getPos2() {
            return pos2;
        }

        public void setPos2(Location pos2) {
            this.pos2 = pos2;
        }

        public void setTask(BukkitTask task) {
            this.task = task;
        }

        public void cancelTask() {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }
}

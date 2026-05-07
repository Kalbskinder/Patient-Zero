package net.kalbskinder.infection.systems;

import org.bukkit.Location;

import java.util.HashMap;

public class LocationSelection {
    private final HashMap<Integer, Location> selectedLocations = new HashMap<>();

    public Location getPos1() {
        return selectedLocations.get(1);
    }

    public Location getPos2() {
        return selectedLocations.get(2);
    }

    public void setPos1(Location location) {
        selectedLocations.put(1, location);
    }

    public void setPos2(Location location) {
        selectedLocations.put(2, location);
    }
}

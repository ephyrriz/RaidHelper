package ru.ephy.raidhelper.raidevents;

import org.bukkit.Location;
import org.bukkit.Raid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Set;

public class RaidManager {

    private static RaidManager instance; // Instance of the class.
    private final HashMap<Location, Raid> raidList = new HashMap<>(); // List of all raids and their locations accordingly.
    private final HashMap<Location, RaidInfo> raidInfo = new HashMap<>(); // List of raids and their counters accordingly.

    public static RaidManager getInstance() {
        if (instance == null) {
            instance = new RaidManager();
        }
        return instance;
    }

    public void addRaid(@NotNull final Location location, @NotNull final Raid raid) {
        raidList.put(location, raid);
        raidInfo.put(location, new RaidInfo());
    }

    @Nullable
    public Raid getRaid(@NotNull final Location location) {
        return raidList.get(location);
    }

    @Nullable
    public RaidInfo getRaidInfo(@NotNull final Location location) {
        return raidInfo.get(location);
    }

    public void removeRaid(@NotNull final Location location) {
        raidList.remove(location);
        raidInfo.remove(location);
    }

    @NotNull
    public Set<Location> getAllRaidLocations() {
        return raidList.keySet();
    }
}

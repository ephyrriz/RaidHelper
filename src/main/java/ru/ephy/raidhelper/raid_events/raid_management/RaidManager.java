package ru.ephy.raidhelper.raid_events.raid_management;

import org.bukkit.Location;
import org.bukkit.Raid;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Set;

public class RaidManager {

    private static RaidManager instance;

    private final HashMap<Location, RaidData> raidDataHashMap = new HashMap<>();

    public static RaidManager getInstance() {
        if (instance == null) {
            instance = new RaidManager();
        }
        return instance;
    }

    public void addRaid(@NotNull final Raid raid, @NotNull final Location raidLocation) {
        final RaidTimeCounter raidTimeCounter = new RaidTimeCounter();
        final RaidData raidData = new RaidData(raid, raidTimeCounter);
        raidDataHashMap.put(raidLocation, raidData);
    }

    public Raid getRaid(@NotNull final Location location) {
        final RaidData raidData = raidDataHashMap.get(location);
        return (raidData != null) ? raidData.getRaid() : null;
    }

    public RaidTimeCounter getRaidTimeCounter(@NotNull final Location location) {
        final RaidData raidData = raidDataHashMap.get(location);
        return (raidData != null) ? raidData.getRaidTimeCounter() : null;
    }

    public void removeRaid(@NotNull final Location location) {
        raidDataHashMap.remove(location);
    }

    public Set<Location> getAllRaidLocations() {
        return raidDataHashMap.keySet();
    }
}

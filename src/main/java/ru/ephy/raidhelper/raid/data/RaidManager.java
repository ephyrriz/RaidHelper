package ru.ephy.raidhelper.raid.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;

import java.util.*;

/**
 * Manages active raids across different worlds,
 * handling their addition, removal, and retrieval.
 *
 * This class optimizes raid handling to avoid redundant
 * operations and ensure proper cleanup when raids are no longer needed.
 */
@Getter
@RequiredArgsConstructor
public class RaidManager {

    // Map of worlds to their active raids
    private final Map<World, Map<Integer, RaidData>> activeRaidsByWorld = new HashMap<>();

    /**
     *
     * Adds a raid if it's not already present and starts any associated logic.
     * If the raid is new, it will be logged and added for tracking.
     *
     * @param raid The Raid instance to be added
     */
    public void addRaidIfAbsent(final Raid raid) {
        final int raidId = raid.getId();
        final Location raidLocation = raid.getLocation();
        final World raidWorld = raidLocation.getWorld();

        activeRaidsByWorld.computeIfAbsent(raidWorld, world -> new HashMap<>())
                          .computeIfAbsent(raidId, id -> {
                              final RaidData raidData = new RaidData(raidId, raid, raidLocation, raidWorld);
                              raidData.setLastUpdatedTime(System.currentTimeMillis() / 50); // Divides by 50 to transate into ticks. 1 tick = 50ms
                              return raidData;
                          });
    }

    /**
     * Removes the raid from the map. If no other raids exist
     * in the world, the world itself is also removed from the map.
     *
     * @param raid The Raid instance to be removed.
     */
    public void removeRaidIfPresent(final Raid raid) {
        final int raidId = raid.getId();
        final World raidWorld = raid.getLocation().getWorld();

        activeRaidsByWorld.computeIfPresent(raidWorld, (world, raidDataMap) -> {
            raidDataMap.remove(raidId);
            return raidDataMap.isEmpty() ? null : raidDataMap;
        });
    }

    /**
     * Checks if a given raid is currently registered.
     *
     * @param raid The Raid instance to check.
     * @return True if the raid is registered, false otherwise.
     */
    public boolean isRaidRegistered(final Raid raid) {
        if (raid == null) return false;

        final int raidId = raid.getId();
        final World raidWorld = raid.getLocation().getWorld();

        final Map<Integer, RaidData> raidDataMap = activeRaidsByWorld.get(raidWorld);

        return raidDataMap != null && raidDataMap.containsKey(raidId);
    }
}

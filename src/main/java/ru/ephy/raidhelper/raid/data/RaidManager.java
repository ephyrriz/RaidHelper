package ru.ephy.raidhelper.raid.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages all active raids across different worlds,
 * handling their addition, removal,
 * and retrieval of associated RaidData instances.
 *
 * This class optimizes raid handling to avoid redundant
 * operations and ensure raids are properly cleaned up
 * when no longer needed.
 */
@RequiredArgsConstructor
public class RaidManager {

    private final Logger logger;                                                      // Logger for debugging
    @Getter
    private final Map<World, Map<Integer, RaidData>> worldRaidMap = new HashMap<>();  // A map of worlds to their active raiding data

    /**
     * Adds a raid if it's not already present in the map
     * and starts any associated logic. If the raid is new,
     * it will be logged and added to the active raids map
     * for tracking.
     *
     * @param raid The Raid instance to be added
     */
    public void registerRaidIfAbsent(final Raid raid) {
        final int raidId = raid.getId();
        final Location raidLocation = raid.getLocation();
        final World raidWorld = raidLocation.getWorld();

        worldRaidMap.computeIfAbsent(raidWorld, world -> new HashMap<>())
                    .computeIfAbsent(raidId, id -> {
                        logger.info("Raid added. RaidId: " + raidId + " | RaidLocation: " + raidLocation);
                        return new RaidData(raidId, raid, raidLocation, raidWorld);
                    });
    }

    /**
     * Removes the raid from the map. If no other raids exist
     * in the world, the world itself is also removed from the map.
     *
     * @param raid The Raid instance to be unregistered.
     */
    public void unregisterRaidIfPresent(final Raid raid) {
        worldRaidMap.computeIfPresent(raid.getLocation().getWorld(), (world, raidDataMap) -> {
            logger.info("Raid removed. RaidId: " + raid.getId() + " | Location: " + raid.getLocation());
            raidDataMap.remove(raid.getId());
            return raidDataMap.isEmpty() ? null : raidDataMap;
        });
    }

    /**
     * Checks whether a given raid is currently registered in the map.
     *
     * @param raid The Raid instance to check for.
     * @return True if the raid is registered, false otherwise.
     */
    public boolean isRaidRegisteredInMap(final Raid raid) {
        if (worldRaidMap.isEmpty()) return false;

        final World raidWorld = raid.getLocation().getWorld();
        final Map<Integer, RaidData> integerRaidDataMap = worldRaidMap.get(raidWorld);

        return integerRaidDataMap != null && integerRaidDataMap.get(raid.getId()) != null;
    }
}

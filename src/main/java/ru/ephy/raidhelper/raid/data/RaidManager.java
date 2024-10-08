package ru.ephy.raidhelper.raid.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages active raids across different worlds,
 * handling their addition, removal, and retrieval.
 *
 * This class optimizes raid handling to avoid redundant
 * operations and ensure proper cleanup when raids are no longer needed.
 */
@RequiredArgsConstructor
public class RaidManager {

    // Logger for debugging
    private final Logger logger;

    // Map of worlds to their active raids
    @Getter
    private final Map<World, Map<Integer, RaidData>> activeRaidsByWorld = new HashMap<>();

    /**
     *
     * Adds a raid if it's not already present and starts any associated logic.
     * If the raid is new, it will be logged and added for tracking.
     *
     * @param raid The Raid instance to be added
     */
    public void addRaidIfAbsent(final Raid raid) {
        if (raid == null) {
            logger.warning("The passed raid to the register is null.");
            return;
        }

        final int raidId = raid.getId();
        final Location raidLocation = raid.getLocation();
        final World raidWorld = raidLocation.getWorld();

        activeRaidsByWorld.computeIfAbsent(raidWorld, world -> new HashMap<>())
                    .computeIfAbsent(raidId, id -> new RaidData(raidId, raid, raidLocation, raidWorld));
    }

    /**
     * Removes the raid from the map. If no other raids exist
     * in the world, the world itself is also removed from the map.
     *
     * @param raid The Raid instance to be removed.
     */
    public void removeRaidIfPresent(final Raid raid) {
        activeRaidsByWorld.computeIfPresent(raid.getLocation().getWorld(), (world, raidDataMap) -> {
            raidDataMap.remove(raid.getId());
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
        final World raidWorld = raid.getLocation().getWorld();
        return activeRaidsByWorld.containsKey(raidWorld) &&
               activeRaidsByWorld.get(raidWorld).containsKey(raid.getId());

    }
}

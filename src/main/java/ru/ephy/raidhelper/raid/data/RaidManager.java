package ru.ephy.raidhelper.raid.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages all active raids across different worlds, handling their addition, removal,
 * and retrieval of associated RaidData instances.
 * <p>
 * This class optimizes raid handling to avoid redundant operations and ensure
 * raids are properly cleaned up when no longer needed.
 */
@RequiredArgsConstructor
public class RaidManager {
    @Getter
    private final Map<World, Map<Integer, RaidData>> worldRaidMap = new HashMap<>();  // A map of worlds to their active raiding data
    private final Logger logger;                                                          // Logger for debugging

    /**
     * Adds a raid if it's not already present in the map and starts any associated logic.
     * If the raid is new, it will be logged and added to the active raids map for tracking.
     *
     * @param raid The Raid instance to be registered.
     */
    public void addRaidIfAbsent(final Raid raid) {
        final Location raidLocation = raid.getLocation();
        final World raidWorld = raidLocation.getWorld();
        final int raidId = raid.getId();

        worldRaidMap.computeIfAbsent(raidWorld, w -> new HashMap<>())
                    .computeIfAbsent(raidId, id -> {
                        logger.info("Raid added. RaidId: " + raidId + " | RaidLocation: " + raidLocation);
                        return new RaidData(raid, raidId, raidLocation, raidWorld);
                    });
    }

    /**
     * Removes the raid from the map. If no other raids exist
     * in the world, the world itself is also removed from the map.
     *
     * @param raid The Raid instance to be unregistered.
     */
    public void removeRaidIfPresent(final Raid raid) {
        final World raidWorld = raid.getLocation().getWorld();
        final int raidId = raid.getId();

        worldRaidMap.computeIfPresent(raidWorld, (world, raidDataMap) -> {
            raidDataMap.remove(raidId);
            logger.info("Raid removed. RaidId: " + raidId + " | Location: " + raid.getLocation());
            return raidDataMap.isEmpty() ? null : raidDataMap; // Clean up empty worlds
        });
    }

    /**
     * Retrieves the RaidData associated with a given raid.
     *
     * @param raid The Raid instance for which data is requested.
     * @return An Optional containing the RaidData, or empty if no data is found.
     */
    @NotNull
    public Optional<RaidData> getRaidData(final Raid raid) {
        final World world = raid.getLocation().getWorld();
        final int raidId = raid.getId();

        return Optional.ofNullable(
                worldRaidMap.getOrDefault(world, Collections.emptyMap()).get(raidId));
    }

    /**
     * Checks whether a given raid is currently registered in the map.
     *
     * @param raid The Raid instance to check for.
     * @return True if the raid is registered, false otherwise.
     */
    public boolean isRaidInMap(final Raid raid) {
        final World world = raid.getLocation().getWorld();
        final int raidId = raid.getId();

        return worldRaidMap.containsKey(world) && worldRaidMap.get(world).containsKey(raidId);
    }
}

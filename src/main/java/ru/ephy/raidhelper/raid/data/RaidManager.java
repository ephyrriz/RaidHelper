package ru.ephy.raidhelper.raid.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.ephy.raidhelper.MayBeRemoved;
import ru.ephy.raidhelper.config.Config;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages all active raids and their associated logic.
 * Responsible for adding, removing, and retrieving RaidData instances.
 */
@Getter
@RequiredArgsConstructor
public class RaidManager {

    private final JavaPlugin plugin;                                                   // Plugin's instance
    private final Config config;                                                       // Holds plugin configuration settings
    private final Logger logger;                                                       // Logger for debugging
    private final Map<World, Map<Integer, RaidData>> worldRaidMap = new HashMap<>();   // A map of worlds to their active raiding data

    /**
     * Adds a raid to the map and starts its associated logic.
     *
     * @param raid The Raid instance to be added.
     */
    public void addRaid(final Raid raid) {
        final Location location = raid.getLocation();
        final World world = location.getWorld();
        final int raidId = raid.getId();
        final RaidData raidData = new RaidData(raid, location, world);

        logger.info("Raid added. RaidId: " + raidId + " | RaidLocation: " + location);
        worldRaidMap.computeIfAbsent(world, w -> new HashMap<>()).put(raidId, raidData);
    }

    /**
     * Removes a raid from the map and stops its associated logic.
     * if no active raids left in the world, it gets removed too.
     *
     * @param raid The raid to be removed.
     */
    public void removeRaid(final Raid raid) {
        final World currentWorld = raid.getLocation().getWorld();
        final int raidId = raid.getId();

        worldRaidMap.computeIfPresent(currentWorld, (world, raidDataMap) -> {
            raidDataMap.remove(raidId);
            return raidDataMap.isEmpty() ? null : raidDataMap;
        });
    }

    /**
     * Retrieves the RaidData instance associated with the given raid.
     * Returns an Optional containing the RaidData if present, or an empty Optional if not found.
     *
     * @param raid The Raid instance whose data is being retrieved.
     * @return An Optional containing the RaidData associated with the raid, or empty if not found.
     */
    @NotNull
    @MayBeRemoved
    public Optional<RaidData> getRaidLogic(final Raid raid) {
        final World world = raid.getLocation().getWorld();
        final int raidId = raid.getId();

        return Optional.ofNullable(
                worldRaidMap.getOrDefault(world, Collections.emptyMap()).get(raidId));
    }

    /**
     * Checks if a raid is already registered in the map.
     *
     * @param raid The Raid instance to check.
     * @return True if the raid is in the map, false otherwise.
     */
    public boolean isRaidInMap(final Raid raid) {
        final World world = raid.getLocation().getWorld();
        final int raidId = raid.getId();

        return worldRaidMap.containsKey(world) && worldRaidMap.get(world).containsKey(raidId);
    }
}

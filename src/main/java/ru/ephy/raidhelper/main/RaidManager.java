package ru.ephy.raidhelper.main;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import ru.ephy.raidhelper.files.Config;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages all active raids and their associated logic.
 * Responsible for adding, removing, and retrieving raid logic instances.
 */
@Getter
@RequiredArgsConstructor
public class RaidManager {

    private final JavaPlugin plugin;                                              // Plugin instance reference
    private final Config config;                                                  // Holds configuration data
    private final Logger logger;                                                  // Logger for logging information
    private final Map<World, Map<Integer, RaidLogic>> raidMap = new HashMap<>();  // A list of raids and their data.

    /**
     * Adds a raid to the map and starts its associated logic.
     *
     * @param raid The raid to be added.
     */
    public void addRaid(final Raid raid) {
        final World world = raid.getLocation().getWorld();
        final int raidId = raid.getId();
        final RaidLogic raidLogic = new RaidLogic(plugin, config, raid, world, logger);

        raidMap.computeIfAbsent(world, k -> new HashMap<>()).put(raidId, raidLogic);
    }

    /**
     * Removes a raid from the map and stops its associated logic.
     * if no active raids left in the world, it gets removed too.
     *
     * @param raid The raid to be removed.
     */
    public void removeRaid(final Raid raid) {
        final World world = raid.getLocation().getWorld();
        final int raidId = raid.getId();

        if (raidMap.containsKey(world)) {
            if (raidMap.get(world) != null) { // Removes the raid from the map
                raidMap.get(world).remove(raidId);
            }
            if (raidMap.get(world).isEmpty()) { // If there are no active raids, the world removes too
                raidMap.remove(world);
            }
        }
    }

    /**
     * Retrieves the RaidLogic instance associated with the given raid.
     * Returns null if the raid is not in the map.
     *
     * @param  raid The raid whose logic is being retrieved.
     * @return The RaidLogic associated with the raid, or null if not found.
     */
    @Nullable
    public RaidLogic getRaidLogic(final Raid raid) {
        final World world = raid.getLocation().getWorld();
        final int raidId = raid.getId();

        if (raidMap.containsKey(world)) {
            if (raidMap.get(world) != null) {
                return raidMap.get(world).get(raidId);
            }
        }

        return null;
    }

    /**
     * Checks if a raid is already registered in the map.
     *
     * @param  raid The raid to check.
     * @return True if the raid is in the map, false otherwise.
     */
    public boolean isRaidInMap(final Raid raid) {
        final World world = raid.getLocation().getWorld();
        final int raidId = raid.getId();

        if (raidMap.containsKey(world)) {
            if (raidMap.get(world) != null) {
                final RaidLogic raidLogic = raidMap.get(world).get(raidId);
                return raidLogic != null;
            }
        }
        return false;
    }
}

package ru.ephy.raidhelper.raids;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Raid;
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

    private final JavaPlugin plugin;                                  // Plugin instance reference
    private final Config config;                                      // Holds configuration data
    private final Logger logger;                                      // Logger for logging information
    private final Map<Integer, RaidLogic> raidMap = new HashMap<>();  // A list of raids and their data.

    /**
     * Adds a raid to the map and starts its associated logic.
     *
     * @param raid The raid to be added.
     */
    public void addRaid(final Raid raid) {
        final RaidLogic raidLogic = new RaidLogic(plugin, config, raid, logger);
        raidLogic.startRaidCounter();
        raidMap.put(raid.getId(), raidLogic);
    }

    /**
     * Removes a raid from the map and stops its associated logic.
     *
     * @param raid The raid to be removed.
     */
    public void removeRaid(final Raid raid) {
        raidMap.get(raid.getId()).stopRaidCounter();
        raidMap.remove(raid.getId());
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
        if (raidMap.containsKey(raid.getId())) {
            return raidMap.get(raid.getId());
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
        logger.info("IsRaidInMap: " + raidMap.containsKey(raid.getId()));
        return raidMap.containsKey(raid.getId());
    }
}

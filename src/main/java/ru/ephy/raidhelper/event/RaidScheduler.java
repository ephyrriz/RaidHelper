package ru.ephy.raidhelper.event;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.files.Config;

import java.util.Map;
import java.util.logging.Logger;

/**
 * This class is responsible for scheduled checks of
 * the active raids in the configured worlds.
 * If a raid is new, it is added to the map.
 */
@RequiredArgsConstructor
public class RaidScheduler {

    private final JavaPlugin plugin;          // Reference to the main plugin instance
    private final RaidManager raidManager;    // Manages raids and their data
    private final Config config;              // Holds configuration data
    private final Logger logger;              // Logger for logging information

    private Map<Raid, RaidData> raidDataMap;  // Stores the mapping of raids to their associated data

    /**
     * Starts the raid scheduler to periodically
     * check for active raids.
     */
    public void startScheduler() {
        // Initializes the raid data map.
        initializeRaidDataMap();
        // Schedules a task.
        Bukkit.getScheduler().runTaskTimer(plugin, this::getActiveRaidsInWorlds, 0, config.getFrequency());
        logger.info("RaidScheduler started successfully.");
    }

    /**
     * Initializes the necessary variables for the scheduler.
     */
    private void initializeRaidDataMap() {
        raidDataMap = raidManager.getRaidMap();
    }

    /**
     * Iterates through the configured worlds to find
     * active raids and analyzes them one by one.
     */
    private void getActiveRaidsInWorlds() {
        config.getWorldList().forEach(world ->
                world.getRaids().forEach(this::addRaidToMap));
    }

    /**
     * Analyzes a raid; if it is not already in the raid data map,
     * it will be added. Otherwise, it is ignored.
     *
     * @param raid The raid to be analyzed.
     */
    private void addRaidToMap(final Raid raid) {
        // If the raid is not already in the map, add it with its data
        if (!raidDataMap.containsKey(raid)) {
            raidDataMap.put(raid, new RaidData(raid.getActiveTicks(), raid.getLocation(), raid));
        }
    }
}

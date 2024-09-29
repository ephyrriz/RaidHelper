package ru.ephy.raidhelper.raid.scheduler;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.logging.Logger;

/**
 * Manages scheduled checks for active raids across all worlds.
 * Handles specific logic based on the state of the raids through the RaidStateManager.
 */
@RequiredArgsConstructor
public class RaidScheduler {

    private final JavaPlugin plugin;           // Plugin's instance
    private final RaidManager raidManager;     // Manages active raids
    private final Config config;               // Holds plugin configuration settings
    private final Logger logger;               // Logger for debugging

    private RaidStateManager raidStateManager; // Manages the states of the raids

    /**
     * Starts the raid event scheduler, running periodic checks on active raids.
     *
     * @throws IllegalArgumentException If an illegal argument was passed during the scheduling process.
     */
    public void startScheduler() throws IllegalArgumentException {
        setupRaidStateManager(); // Initialize the RaidStateManager
        Bukkit.getScheduler().runTaskTimer( // Schedule periodic checks on active raids
                plugin, this::checkActiveRaids, 0, config.getRaidCheckFrequency());
    }

    /**
     * Sets up the RaidStateManager with necessary configuration and initializes it.
     */
    private void setupRaidStateManager() {
        raidStateManager = new RaidStateManager(config, logger); // Create new instance
        raidStateManager.initialize(); // Initialize with config values
    }

    /**
     * Checks the status of active raids in all worlds and updates their state.
     * This method iterates over all active raids and updates their respective RaidData.
     */
    private void checkActiveRaids() {
        raidManager.getWorldRaidMap().forEach((world, raidDataMap) ->
                raidDataMap.forEach((raidId, raidData) -> updateRaidState(raidData)));
    }

    /**
     * Updates the state of the given raid using the RaidStateManager.
     *
     * @param raidData The RaidData instance that holds information about the current raid.
     */
    private void updateRaidState(final RaidData raidData) {
        raidStateManager.updateRaidState(raidData); // Delegate to RaidStateManager
    }
}

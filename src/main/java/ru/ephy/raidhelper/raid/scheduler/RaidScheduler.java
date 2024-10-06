package ru.ephy.raidhelper.raid.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The RaidScheduler class is responsible for periodically
 * checking the status of active raids in all specified worlds.
 * It processes raids incrementally across multiple ticks to avoid
 * overloading the server with intensive raid-check tasks in a single
 * tick. The class queues raid data for processing and ensures each
 * raid's state is updated as necessary using RaidStateManager.
 */
public class RaidScheduler {

    private final JavaPlugin plugin;                 // Reference to the plugin instance
    private final RaidManager raidManager;           // Manages raid data and maps across worlds

    private final RaidStateManager raidStateManager; // Manages and updates the state of raids
    private final Queue<RaidData> raidDataQueue;     // Queue that stores raids awaiting processing
    private final Set<World> worldsToCheck;          // Set of worlds where raids will be checked
    private final int maxRaidsPerTick;               // Maximum number of raid checks to perform in a single tick

    /**
     * Constructs the {@link RaidScheduler} for periodical raids states updates
     *
     * @param plugin       The JavaPlugin instance managing this scheduler
     * @param raidManager  The manager responsible for tracking active raids
     * @param config       Configuration settings for scheduling checks
     * @param logger       Logger instance for debugging
     */
    public RaidScheduler(final JavaPlugin plugin, final RaidManager raidManager,
                         final Config config, final Logger logger) {
        this.plugin = plugin;
        this.raidManager = raidManager;

        raidStateManager = new RaidStateManager(config, logger);
        raidDataQueue = new LinkedList<>();
        worldsToCheck = config.getValidWorlds();
        maxRaidsPerTick = config.getMaxChecksPerTick();

        Bukkit.getScheduler().runTaskTimer( // Schedule periodic checks on active raids
                plugin,
                this::checkActiveRaids,
                0L,
                20L
        );
    }

    /**
     * Iterates through all defined worlds and queues
     * all active raids for state checks. This method
     * retrieves active raids from each world and adds
     * them to a queue if they are not already queued,
     * ensuring that no raid is processed more than
     * necessary.
     */
    private void checkActiveRaids() {
        for (final World world : worldsToCheck) {
            final Map<Integer, RaidData> raidDataMap = raidManager.getWorldRaidMap().get(world);

            if (raidDataMap != null) {
                for (final RaidData raidData : raidDataMap.values()) {
                    if (!raidDataQueue.contains(raidData)) {
                        raidDataQueue.offer(raidData);
                    }
                }
            }
        }
        processQueuedRaids(); // Start processing raids after collecting them
    }

    /**
     * Processes a limited number of raids from the queue
     * each tick to avoid overloading the server.
     * This method polls the queue and updates the state of each
     * raid, re-queuing them for further checks in future ticks.
     * If there are more raids to process than the per-tick limit, it
     * schedules another task for the next tick to continue processing.
     */
    private void processQueuedRaids() {
        int processedCount = 0;

        while (processedCount < maxRaidsPerTick && !raidDataQueue.isEmpty()) {
            final RaidData raidData = raidDataQueue.poll();
            updateRaidState(raidData);
            processedCount++;
        }

        if (!raidDataQueue.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(
                    plugin,
                    this::processQueuedRaids,
                    1L
            );
        }
    }

    /**
     * Updates the state of the given raid using the RaidStateManager.
     *
     * @param raidData The RaidData instance representing the current raid.
     */
    private void updateRaidState(final RaidData raidData) {
        raidStateManager.updateRaidState(raidData); // Delegate the state update to RaidStateManager
    }
}

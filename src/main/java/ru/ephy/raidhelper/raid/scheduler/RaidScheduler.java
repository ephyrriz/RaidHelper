package ru.ephy.raidhelper.raid.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;
import ru.ephy.raidhelper.raid.data.RaidManager;
import ru.ephy.raidhelper.raid.scheduler.raidstatemanager.NotificationManager;
import ru.ephy.raidhelper.raid.scheduler.raidstatemanager.RaidCacheManager;
import ru.ephy.raidhelper.raid.scheduler.raidstatemanager.RaidStateManager;
import ru.ephy.raidhelper.raid.scheduler.raidstatemanager.RaidWaveProcessor;

import java.util.*;
import java.util.logging.Logger;

/**
 * Periodically checks and processes active raids
 * in monitored worlds. Raids are processed incrementally
 * across multiple ticks to avoid server lag.
 */
public class RaidScheduler {

    private final JavaPlugin plugin;                 // Plugin reference for scheduling
    private final RaidManager raidManager;           // Manages active raids across worlds
    private final Logger logger;                     // Logger for debugging

    private final RaidStateManager raidStateManager; // Handles raid state updates
    private final Set<RaidData> raidSet;             // Set to ensure no duplicate raids are queued
    private final Set<World> monitoredWorlds;        // Set of worlds where raids are monitored
    private final int raidBatchLimit;                // Max number of raids to process per tick

    private int taskId = -1;                         // Task ID for the scheduler

    /**
     * Initializes the RaidScheduler for periodically processing raids.
     *
     * @param plugin      The JavaPlugin instance
     * @param raidManager Manages raid data across worlds
     * @param config      Configuration for scheduling and raid checks
     * @param logger      Logger for debugging and info
     */
    public RaidScheduler(final JavaPlugin plugin, final RaidManager raidManager,
                         final Config config, final Logger logger) {
        // Initialize required instances
        this.plugin = plugin;
        this.raidManager = raidManager;
        this.logger = logger;

        // Initalize required variables
        monitoredWorlds = config.getValidWorlds();
        raidBatchLimit = config.getMaxChecksPerTick();

        raidSet = new HashSet<>();

        // Initialize Raid State Manager
        raidStateManager = new RaidStateManager(
                new RaidCacheManager(plugin, config),
                new RaidWaveProcessor(config),
                new NotificationManager(config)
        );

        // Start the scheduler
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::queueActiveRaids,
                0L,
                20L
        );
    }

    /**
     * Queues active raids from monitored worlds
     * for state checking.
     */
    private void queueActiveRaids() {
        for (final World world : monitoredWorlds) {
            raidManager.getActiveRaidsByWorld().computeIfPresent(world, (w, raidDataMap) -> {
                raidSet.addAll(raidDataMap.values());
                return raidDataMap;
            });
        }

        if (!raidSet.isEmpty() && taskId == -1) {
            taskId = Bukkit.getScheduler().runTaskTimer(
                    plugin, this::processRaidQueue, 0L, 1L
            ).getTaskId();
        } else if (taskId != -1) {
            logger.warning("Cannot process raid queue because the scheduler is busy. TaskId: " + taskId);
        }
    }

    /**
     * Processes a limited number of raids from the
     * queue per tick to avoid server overload.
     * If there are still raids left in the queue,
     * it schedules another task for the next tick.
     */
    private void processRaidQueue() {
        final Iterator<RaidData> raidDataIterator = raidSet.iterator();
        int processedCount = 0;

        while (processedCount < raidBatchLimit && raidDataIterator.hasNext()) {
            final RaidData raidData = raidDataIterator.next();
            updateRaidState(raidData);
            raidSet.remove(raidData);
            processedCount++;
        }

        if (!raidDataIterator.hasNext() && taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        } else if (taskId == -1) {
            logger.warning("Cannot cancel the process of the raid queue because the scheduler is asleep. TaskId: " + taskId);
        }
    }

    /**
     * Updates the state of the given raid using the RaidStateManager.
     *
     * @param raidData The raid data to update.
     */
    private void updateRaidState(final RaidData raidData) {
        raidStateManager.updateRaidState(raidData); // Delegate the state update to RaidStateManager
    }
}

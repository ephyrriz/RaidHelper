package ru.ephy.raidhelper.raid.monitor;

import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.*;
import java.util.logging.Logger;

/**
 * Monitors specified worlds for active raids
 * and periodically registers them in the RaidManager.
 */
public class RaidSchedulerMonitor {

    private final JavaPlugin plugin;           // Plugin instance for task scheduling
    private final RaidManager raidManager;     // Manages raid registrations
    private final Logger logger;               // Logger for debugging

    private final Set<Raid> raidSet;           // Set to ensure no duplicate raids are queued
    private final Set<World> monitoredWorlds;  // Worlds currently monitored for raids
    private final int raidBatchLimit;          // Max number of raids processed per update

    private int taskId = -1;                   // Task ID for the scheduler

    /**
     * Initializes the RaidMonitor to track and process raids.
     *
     * @param plugin      The JavaPlugin instance
     * @param raidManager The RaidManager instance
     * @param config      The Config instance for settings
     * @param logger      The Logger instance for logging
     */
    public RaidSchedulerMonitor(final JavaPlugin plugin, final RaidManager raidManager,
                                final Config config, final Logger logger) {
        // Initialize required instances
        this.plugin = plugin;
        this.raidManager = raidManager;
        this.logger = logger;

        // Initialize required variables
        monitoredWorlds = config.getValidWorlds();
        raidBatchLimit = config.getMaxChecksPerTick();

        raidSet = new HashSet<>();

        // Start the scheduler
        Bukkit.getScheduler().runTaskTimer(
                plugin, this::scanWorldsForRaids, 0L, config.getWorldCheckFrequency());
    }

    /**
     * Scans all monitored worlds for active
     * raids and enqueues them for processing.
     */
    private void scanWorldsForRaids() {
        for (final World world : monitoredWorlds) {
            final Set<Raid> raidsInWorld = new HashSet<>(world.getRaids());

            for (final Raid raid : raidsInWorld) {
                if (!raidManager.isRaidRegistered(raid)) {
                    raidSet.add(raid);
                }
            }
        }

        if (!raidSet.isEmpty() && taskId == -1) {
            taskId = Bukkit.getScheduler().runTaskTimer(
                    plugin, this::processRaids, 0L, 1L
            ).getTaskId();
        } else if (taskId != -1) {
            logger.warning("Cannot scan raids because the scheduler is busy. TaskId: " + taskId);
        }
    }

    /**
     * Processes active raids in manageable batches.
     */
    private void processRaids() {
        final Iterator<Raid> raidIterator = raidSet.iterator();
        int processedCount = 0;

        while (processedCount < raidBatchLimit && raidIterator.hasNext()) {
            final Raid raid = raidIterator.next();
            registerRaid(raid);
            raidSet.remove(raid);
            processedCount++;
        }

        if (!raidIterator.hasNext() && taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        } else if (taskId == -1) {
            logger.warning("Cannot cancel the task for raids scan because the scheduler is asleep. TaskId: " + taskId);
        }
    }

    /**
     * Registers a raid with the RaidManager if it is not already registered.
     *
     * @param raid The raid to register
     */
    private void registerRaid(final Raid raid) {
        raidManager.addRaidIfAbsent(raid);
    }
}

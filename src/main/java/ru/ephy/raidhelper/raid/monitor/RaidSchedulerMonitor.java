package ru.ephy.raidhelper.raid.monitor;

import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
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

    private final Queue<Integer> raidQueue;       // Queue of active raids to process
    private final Set<Integer> raidSet;           // Set to ensure no duplicate raids are queued
    private final Set<World> monitoredWorlds;  // Worlds currently monitored for raids
    private final int raidBatchLimit;          // Max number of raids processed per update

    private int taskId = -1;                   // Task ID for the scheduler

    /**
     * Initializes the RaidMonitor to track and process raids.
     *
     * @param plugin       The JavaPlugin instance
     * @param raidManager  The RaidManager instance
     * @param config       The Config instance for settings
     * @param logger       The Logger instance for logging
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

        raidQueue = new LinkedList<>();
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
            for (final Raid raid : world.getRaids()) {
                final int raidId = raid.getId();

                if (raidSet.add(raidId) && !raidManager.isRaidRegistered(raid)) {
                    raidQueue.offer(raidId);
                }
            }
        }

        if (!raidQueue.isEmpty()) {
            if (taskId == -1) {
                taskId = Bukkit.getScheduler().runTaskTimer(
                        plugin,
                        this::processRaids,
                        0L,
                        1L
                ).getTaskId();
            } else {
                logger.warning("Cannot scan raids because the scheduler is busy. TaskId: " + taskId);
            }
        }
    }

    /**
     * Processes active raids in manageable batches.
     */
    private void processRaids(final World world) {
        int processedCount = 0;

        while (processedCount < raidBatchLimit && !raidQueue.isEmpty()) {
            final int raidId = raidQueue.poll();
            final Raid raid = getRaid(world, raidId);

            registerRaid(raid);
            raidSet.remove(raidId);
            processedCount++;
        }

        if (raidQueue.isEmpty()) {
            if (taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
                taskId = -1;
            } else {
                logger.warning("Cannot cancel the task for raids scan because the scheduler is asleep. TaskId: " + taskId);
            }
        }
    }

    /**
     * Gets the raid by ID from the specified world.
     *
     * @param world  World to look for the raid
     * @param raidId The ID of the raid
     * @return An Optional containing the raid if found, otherwise empty
     */
    @Nullable
    private Raid getRaid(final World world, final int raidId) {
        return world.getRaid(raidId);
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

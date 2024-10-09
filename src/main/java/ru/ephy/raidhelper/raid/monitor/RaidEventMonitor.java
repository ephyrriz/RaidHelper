package ru.ephy.raidhelper.raid.monitor;

import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.*;
import java.util.logging.Logger;

/**
 * Listens for and processes raid-related events,
 * handling the registration of active raids using the RaidManager.
 */
public class RaidEventMonitor implements Listener {

    private final JavaPlugin plugin;          // Plugin instance for scheduling tasks
    private final RaidManager raidManager;    // Manages raid-related operations
    private final Logger logger;              // Logger for debugging

    private final Queue<Integer> raidQueue;      // Queue for raids being processed
    private final Set<Integer> raidSet;          // Set to ensure no duplicate raids are queued
    private final Set<World> monitoredWorlds; // Worlds that are monitored for raid activity
    private final int raidBatchLimit;         // Maximum number of raids processed per tick

    private int taskId = -1;                  // Task ID for the scheduler

    /**
     * Initializes the RaidEventMonitor to listen and handle raid events.
     *
     * @param plugin      Main plugin instance
     * @param raidManager RaidManager responsible for handling raid registration
     * @param config      Config object to retrieve world and raid processing settings
     */
    public RaidEventMonitor(final JavaPlugin plugin, final RaidManager raidManager,
                            final Config config, final Logger logger) {
        // Initializes required instances
        this.plugin = plugin;
        this.raidManager = raidManager;
        this.logger = logger;

        // Initializes required variables
        monitoredWorlds = config.getValidWorlds();
        raidBatchLimit = config.getMaxChecksPerTick();

        raidQueue = new LinkedList<>();
        raidSet = new HashSet<>();
    }

    /**
     * Handles any raid-related event by
     * scanning the world for raids.
     *
     * @param event Raid-related event
     */
    @EventHandler
    public void on(final RaidEvent event) {
        final World world = event.getWorld();
        scanWorldForRaids(world);
    }

    /**
     * Checks the provided world for active raids
     * if it's in the monitored list.
     *
     * @param world World where the event occurred
     */
    private void scanWorldForRaids(final World world) {
        if (monitoredWorlds.contains(world)) {
            processRaidsInWorld(world);
        }
    }

    /**
     * Adds active raids from the specified
     * world to the queue for processing.
     *
     * @param world World to scan for raids
     */
    private void processRaidsInWorld(final World world) {
        final Set<Raid> raidsInWorld = new HashSet<>(world.getRaids());

        for (final Raid raid : raidsInWorld) {
            final int raidId = raid.getId();

            if (raidManager.isRaidRegistered(getRaid(world, raidId)) && raidSet.add(raidId)) {
                raidQueue.offer(raidId);
            }
        }

        if (!raidQueue.isEmpty() && taskId == -1) {
            taskId = Bukkit.getScheduler().runTaskTimer(
                    plugin, () -> processRaidsInBatches(world), 0L, 1L
            ).getTaskId();
        } else if (taskId != -1) {
            logger.warning("Cannot scan raids because the scheduler is busy. TaskId: " + taskId);
        }
    }

    /**
     * Processes raids in the queue in batches,
     * limiting the number per tick.
     */
    private void processRaidsInBatches(final World world) {
        int processedCount = 0;

        while (processedCount < raidBatchLimit && !raidQueue.isEmpty()) {
            final int raidId = raidQueue.poll();
            final Raid raid = getRaid(world, raidId);

            if (raid != null) {
                registerRaid(raid);
                processedCount++;
            } else {
                logger.warning("The raid by id " + raidId + " is null");
            }
        }

        if (raidQueue.isEmpty() && taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        } else if (taskId == -1) {
            logger.warning("Cannot cancel the task for raids scan because the scheduler is asleep. TaskId: " + taskId);
        }
    }

    /**
     * Gets the raid by ID from the specified world.
     *
     * @param world  World to look for the raid
     * @param raidId The ID of the raid
     * @return The raid instance, if not null. Returns null otherwise.
     */
    @Nullable
    private Raid getRaid(final World world, final int raidId) {
        return world.getRaid(raidId);
    }

    /**
     * Registers the raid in the RaidManager if
     * it's not already registered.
     *
     * @param raid The raid to register
     */
    private void registerRaid(final Raid raid) {
        raidManager.addRaidIfAbsent(raid);
    }
}

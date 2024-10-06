package ru.ephy.raidhelper.raid.monitor;

import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.*;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.*;

/**
 * Listens for and processes raid-related events,
 * handling the registration of active raids using the RaidManager.
 */
public class RaidEventMonitor implements Listener {

    private final JavaPlugin plugin;          // Plugin instance for scheduling tasks
    private final RaidManager raidManager;    // Manages raid-related operations

    private final Queue<Raid> raidQueue;      // Queue for raids being processed
    private final Set<World> monitoredWorlds; // Worlds that are monitored for raid activity
    private final int raidBatchLimit;         // Maximum number of raids processed per tick

    private int taskId = -1;                  // Task ID for the scheduler

    /**
     * Initializes the RaidEventMonitor to listen and handle raid events.
     *
     * @param plugin     Main plugin instance
     * @param raidManager    RaidManager responsible for handling raid registration
     * @param config     Config object to retrieve world and raid processing settings
     */
    public RaidEventMonitor(final JavaPlugin plugin, final RaidManager raidManager,
                            final Config config) {
        this.plugin = plugin;
        this.raidManager = raidManager;

        monitoredWorlds = config.getValidWorlds();
        raidBatchLimit = config.getMaxChecksPerTick();

        raidQueue = new LinkedList<>();
    }

    /**
     * Handles any raid-related event by
     * scanning the world for raids.
     *
     * @param event Raid-related event
     */
    @EventHandler
    public void on(final RaidEvent event) { scanWorldsForRaids(event.getWorld()); }

    /**
     * Checks the provided world for active raids
     * if it's in the monitored list.
     *
     * @param world World where the event occurred
     */
    private void scanWorldsForRaids(final World world) {
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
        raidQueue.addAll(world.getRaids());

        if (!raidQueue.isEmpty() && taskId == -1) {
            taskId = Bukkit.getScheduler().runTaskTimer(
                    plugin,
                    this::processRaidsInBatches,
                    0L,
                    1L
            ).getTaskId();
        }
    }

    /**
     * Processes raids in the queue in batches,
     * limiting the number per tick.
     */
    private void processRaidsInBatches() {
        int processedCount = 0;

        while (processedCount < raidBatchLimit && !raidQueue.isEmpty()) {
            final Raid raid = raidQueue.poll();
            if (raid != null && raid.isStarted()) {
                registerRaid(raid);
                processedCount++;
            }
        }

        if (raidQueue.isEmpty() && taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
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

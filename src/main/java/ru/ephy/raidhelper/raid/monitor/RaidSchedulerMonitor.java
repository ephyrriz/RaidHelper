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

    private final JavaPlugin plugin;              // Plugin instance for scheduling tasks
    private final RaidManager raidManager;        // Manages raid registration
    private final Config config;                  // Configuration settings
    private final Logger logger;                  // Logger for debugging

    private final Queue<Raid> raidQueue;          // List of active raids in the current world
    private final Set<World> monitoredWorlds;     // Worlds being monitored for raids
    private final int maxRaidsPerTick;            // Maximum number of raids processed per tick

    /**
     * Constructs a {@link  RaidSchedulerMonitor} to monitor and process raids.
     *
     * @param plugin       The JavaPlugin instance
     * @param raidManager  The RaidManager instance
     * @param config       The Config instance for settings
     */
    public RaidSchedulerMonitor(final JavaPlugin plugin, final RaidManager raidManager,
                                final Config config, final Logger logger) {
        this.plugin = plugin;
        this.raidManager = raidManager;
        this.config = config;
        this.logger = logger;

        monitoredWorlds = config.getValidWorlds();
        maxRaidsPerTick = config.getMaxChecksPerTick();

        raidQueue = new LinkedList<>();

        Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::checkRaidsInWorlds,
                0L,
                config.getWorldCheckFrequency());
    }

    /**
     * Checks all monitored worlds for active raids
     * and initiates processing if any are found.
     */
    private void checkRaidsInWorlds() {
        for (final World world : monitoredWorlds) {
            for (final Raid raid : world.getRaids()) {
                if (!raidQueue.contains(raid) && !raidManager.isRaidRegisteredInMap(raid)) {
                    raidQueue.offer(raid);
                }
            }
        }

        if (!raidQueue.isEmpty()) {
            processRaidsInWorld();
        }
    }

    /**
     * Loads the active raids in the specified world
     * and processes them in manageable batches.
     */
    private void processRaidsInWorld() {
        int processedCount = 0;

        while (processedCount < maxRaidsPerTick && !raidQueue.isEmpty()) {
            final Raid raid = raidQueue.poll();
            logger.info("Processing Raid: " + (raid != null ? raid.getId() : "null"));

            if (raid != null && raid.isStarted()) {
                registerRaid(raid);
                processedCount++;
            }
        }

        if (!raidQueue.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, this::processRaidsInWorld, 1L);
        }
    }

    /**
     * Registers a raid with the RaidManager if it is not already registered.
     *
     * @param raid The raid to register
     */
    private void registerRaid(final Raid raid) {
        raidManager.registerRaidIfAbsent(raid);
    }
}

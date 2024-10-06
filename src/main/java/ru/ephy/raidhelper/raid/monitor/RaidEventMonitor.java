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
 * Monitors raid-related events and processes active raids
 * by registering them in the RaidManager.
 */
public class RaidEventMonitor implements Listener {

    private final JavaPlugin plugin;            // Plugin instance for scheduling
    private final RaidManager raidManager;      // Manages raid registration

    private final Queue<Raid> raidQueue;        // List of active raids in the current world
    private final Set<World> processingWorlds;  // Tracks worlds currently processing raids
    private final Set<World> monitoredWorlds;   // Worlds to be monitored for active raids
    private final int maxRaidsPerTick;          // Maximum number of raids processed per tick

    /**
     * Constructs the {@link RaidEventMonitor} for managing raid events.
     *
     * @param plugin       JavaPlugin instance
     * @param raidManager  Manages raid operations
     * @param config       Config instance for retrieving settings
     */
    public RaidEventMonitor(final JavaPlugin plugin, final RaidManager raidManager,
                            final Config config) {
        this.plugin = plugin;
        this.raidManager = raidManager;

        monitoredWorlds = config.getValidWorlds();
        maxRaidsPerTick = config.getMaxChecksPerTick();

        raidQueue = new LinkedList<>();
        processingWorlds = new HashSet<>();
    }

    /**
     * Handles the event when a raid is triggered,
     * finished, stopped, or when a new wave spawns.
     *
     * @param event RaidEvent
     */
    @EventHandler
    public void on(final RaidEvent event) { scanWorldForRaids(event.getWorld()); }

    /**
     * Scans all monitored worlds for active raids.
     *
     * @param world World where the event occured
     */
    private void scanWorldForRaids(final World world) {
        if (monitoredWorlds.contains(world)) {
            processWorldRaids(world);
        }
    }

    /**
     * Checks a world for active raids and processes them.
     *
     * @param world The world to check for raids.
     */
    private void processWorldRaids(final World world) {
        raidQueue.addAll(world.getRaids());

        if (!raidQueue.isEmpty()) {
            processRaidsInBatches();
        }
    }

    /**
     * Processes raids in batches, limiting the number per tick.
     */
    private void processRaidsInBatches() {
        int processedCount = 0;

        while (processedCount < maxRaidsPerTick && !raidQueue.isEmpty()) {
            final Raid raid = raidQueue.poll();
            if (raid != null && raid.isStarted()) {
                registerRaid(raid);
                processedCount++;
            }
        }

        if (!raidQueue.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, this::processRaidsInBatches, 1L);
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

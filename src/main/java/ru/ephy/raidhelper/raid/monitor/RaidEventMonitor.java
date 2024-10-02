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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Monitors raid-related events and processes active raids
 * by registering them in the RaidManager.
 */
public class RaidEventMonitor implements Listener {

    private final JavaPlugin plugin;            // Plugin instance for scheduling
    private final RaidManager raidManager;      // Manages raid registration

    private final List<Raid> currentRaids;      // List of active raids in the current world
    private final Set<World> processingWorlds;  // Tracks worlds currently processing raids
    private final Set<World> monitoredWorlds;   // Worlds to be monitored for active raids
    private final int maxRaidsPerTick;          // Maximum number of raids processed per tick

    private int raidIndex = 0;                  // Tracks current raid being processed

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

        monitoredWorlds = config.getWorldSet();
        maxRaidsPerTick = config.getMaxChecksPerTick();

        currentRaids = new ArrayList<>();
        processingWorlds = new HashSet<>();
    }

    /**
     * Handles the event when a raid is triggered,
     * finished, stopped, or when a new wave spawns.
     *
     * @param event RaidEvent
     */
    @EventHandler
    public void on(final RaidEvent event) { scanWorldsForRaids(event.getWorld()); }

    /**
     * Scans all monitored worlds for active raids.
     *
     * @param world World where the event occured
     */
    private void scanWorldsForRaids(final World world) {
        if (monitoredWorlds.contains(world) && !processingWorlds.contains(world)) {
            processWorldRaids(world);
        }
    }

    /**
     * Checks a world for active raids and processes them.
     *
     * @param world The world to check for raids.
     */
    private void processWorldRaids(final World world) {
        currentRaids.clear();
        currentRaids.addAll(world.getRaids());

        if (!currentRaids.isEmpty()) {
            raidIndex = 0;
            processingWorlds.add(world);
            processRaidsInBatches(world);
        }
    }

    /**
     * Processes raids in batches, limiting the number per tick.
     *
     * @param world The world currently being processed.
     */
    private void processRaidsInBatches(final World world) {
        int processedRaids = 0;

        while (processedRaids < maxRaidsPerTick && raidIndex < currentRaids.size()) {
            registerRaid(currentRaids.get(raidIndex));
            raidIndex++;
            processedRaids++;
        }

        if (raidIndex < currentRaids.size()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> processRaidsInBatches(world), 1L);
        } else {
            processingWorlds.remove(world);
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

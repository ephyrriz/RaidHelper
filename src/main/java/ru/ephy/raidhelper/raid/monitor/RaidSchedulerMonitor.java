package ru.ephy.raidhelper.raid.monitor;

import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.List;

/**
 * This class serves to regularly
 * check valid worlds for active raids
 * in them to send these raids in the
 * raidMap.
 */
public class RaidSchedulerMonitor {

    private final JavaPlugin plugin;        // To run the scheduler
    private final RaidManager raidManager;  // To get the needed methods of this class
    private final Config config;            // To get needed values from the config
    private final List<World> worldList;    // To use for the checkActiveRaidsInWorlds method
    private final int maxChecksPerTick;     // To limit number of checked raids per tick

    private List<Raid> raidList;            // To process the list of raids of a world
    private int currentCheckIndex = 0;      // To count current number of checks

    /**
     * Constructs the class for a proper work.
     *
     * @param plugin       Plugin's instance
     * @param raidManager  RaidManager's instance
     * @param config       Config's instance
     */
    public RaidSchedulerMonitor(final JavaPlugin plugin, final RaidManager raidManager, final Config config) {
        this.plugin = plugin;
        this.raidManager = raidManager;
        this.config = config;
        worldList = config.getWorldList();
        maxChecksPerTick = config.getMaxChecksPerTick();
    }

    /**
     * Starts the raid monitoring scheduler. The scheduler
     * periodically checks worlds for active raids
     * and processes them in batches to ensure server performance.
     */
    public void startMonitor() {
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::checkActiveRaidsInWorlds,
                0,
                config.getWorldCheckFrequency());
    }

    /**
     * Check worlds for active raids in them;
     * passes active raids to the addRaidToMap method.
     */
    private void checkActiveRaidsInWorlds() {
        raidList.clear();

        for (final World world : worldList) {
            raidList = world.getRaids();
            if (!raidList.isEmpty()) {
                currentCheckIndex = 0;
                processRaidList();
            }
        }
    }

    /**
     * Processes the current list of raids in batches,
     * with a limit on the number of raids processed
     * per tick to avoid excessive strain on server resources.
     */
    private void processRaidList() {
        int processedCount = 0; // Track how many raids have been processed

        while (processedCount < maxChecksPerTick && currentCheckIndex < raidList.size()) {
            final Raid raid = raidList.get(currentCheckIndex);
            addRaidToMap(raid);
            currentCheckIndex++;
            processedCount++;
        }

        // If not all raids were processed, schedule the next batch to run after 1 tick
        if (currentCheckIndex < raidList.size()) {
            Bukkit.getScheduler().runTaskLater(plugin, this::processRaidList, 1);
        }
    }

    /**
     * Adds a raid to the map; if it is not already in the raid
     * data map, it will be added. Otherwise, it is ignored.
     *
     * @param raid The raid to be added.
     */
    private void addRaidToMap(final Raid raid) {
        raidManager.addRaidIfAbsent(raid);
    }
}

package ru.ephy.raidhelper.raid.monitor;

import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class serves to detect multiple of
 * events related to raids as key moments
 * to check worlds for any active raids
 * and input them in the RaidManager's map.
 */
public class RaidEventMonitor implements Listener {

    private final JavaPlugin plugin;       // Plugin's instance
    private final RaidManager raidManager; // To get the needed methods of this class
    private final Set<World> worldSet;   // To use for the checkActiveRaidsInWorlds method
    private final int maxChecksPerTick;    // To limit number of checked raids per tick

    private List<Raid> raidList;           // To process the list of raids of a world
    private int currentCheckIndex = 0;     // To count current number of checks

    /**
     * Constructs the class for a proper work.
     *
     * @param raidManager  RaidManager class instance
     * @param config       Config class instance
     */
    public RaidEventMonitor(final JavaPlugin plugin, final RaidManager raidManager, final Config config) {
        this.plugin = plugin;
        this.raidManager = raidManager;
        worldSet = config.getWorldSet();
        maxChecksPerTick = config.getMaxChecksPerTick();
        raidList = new ArrayList<>();
    }

    /**
     * Is being called when a raid starts.
     *
     * @param event RaidTriggerEvent
     */
    @EventHandler
    public void on(final RaidTriggerEvent event){
        checkActiveRaidsInWorlds();
    }

    /**
     * Is being called when a raid's wave spawns.
     *
     * @param event RaidSpawnWaveEvent
     */
    @EventHandler
    public void on(final RaidSpawnWaveEvent event) {
        checkActiveRaidsInWorlds();
    }

    /**
     * Is being called when a raid finishes.
     *
     * @param event RaidFinishEvent
     */
    @EventHandler
    public void on(final RaidFinishEvent event) {
        checkActiveRaidsInWorlds();
    }

    /**
     * Is being called when a raid stops.
     *
     * @param event RaidStopEvent
     */
    @EventHandler
    public void on(final RaidStopEvent event) {
        checkActiveRaidsInWorlds();
    }

    /**
     * Check worlds for active raids in them;
     * passes active raids to the addRaidToMap method.
     */
    private void checkActiveRaidsInWorlds() {
        raidList.clear();

        for (final World world : worldSet) {
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
        raidManager.registerRaidIfAbsent(raid);
    }
}

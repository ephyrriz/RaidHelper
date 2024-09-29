package ru.ephy.raidhelper.raid.monitor;

import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.List;

/**
 * This class serves to detect multiple of
 * events related to raids as key moments
 * to check worlds for any active raids
 * and input them in the RaidManager's map.
 */
public class RaidEventMonitor implements Listener {

    private final RaidManager raidManager; // To get the needed methods of this class
    private final List<World> worldList;   // To use for the checkActiveRaidsInWorlds method

    /**
     * Constructs the class for a proper work.
     *
     * @param raidManager  RaidManager class instance
     * @param config       Config class instance
     */
    public RaidEventMonitor(final RaidManager raidManager, final Config config) {
        this.raidManager = raidManager;
        worldList = config.getWorldList();
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
        for (final World world : worldList) {
            final List<Raid> raidList = world.getRaids();
            if (!raidList.isEmpty()) {
                for (final Raid raid : raidList) {
                    addRaidToMap(raid);
                }
            }
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

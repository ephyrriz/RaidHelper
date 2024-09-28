package ru.ephy.raidhelper.main;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.files.Config;

import java.util.logging.Logger;

/**
 * This class is responsible for scheduled checks of
 * the active raids in the configured worlds.
 * If a raid is new, it is added to the map.
 */
@RequiredArgsConstructor
public class RaidMonitor {

    private final JavaPlugin plugin;        // Plugin instance reference
    private final RaidManager raidManager;  // RaidManager instance
    private final Config config;            // Config instance
    private final Logger logger;            // Logger for logging information

    /**
     * Starts the raid scheduler to periodically
     * check for active raids.
     *
     * @throws IllegalArgumentException When an illegal argument was passed to the scheduler.
     */
    public void startScheduler() throws IllegalArgumentException {
        try {
            Bukkit.getScheduler().runTaskTimer(plugin, this::getActiveRaidsInWorlds, 0, config.getFrequencyWorld());
        } catch (final Exception e) {
            logger.severe("An error occured during starting the scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Iterates through the configured worlds to find
     * active raids and analyzes them one by one.
     */
    private void getActiveRaidsInWorlds() {
        for (final World world : config.getWorldList()) {
            for (final Raid raid : world.getRaids()) {
                addRaidToMap(raid);
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
        // If the raid is not already in the map, add it with its data
        if (!raidManager.isRaidInMap(raid)) {
            raidManager.addRaid(raid);
        }
    }
}

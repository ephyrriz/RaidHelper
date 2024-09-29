package ru.ephy.raidhelper.raid.monitor;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.List;

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

    private List<World> worldList;

    /**
     * Starts the raid scheduler to periodically
     * check for active raids.
     *
     * @throws IllegalArgumentException If an illegal argument was passed during the scheduling process.
     */
    public void startMonitor() throws IllegalArgumentException {
        initialize();
        Bukkit.getScheduler().runTaskTimer(plugin, this::getActiveRaidsInWorlds, 0, config.getWorldCheckFrequency());
    }

    private void initialize() {
        worldList = config.getWorldList();
    }

    /**
     * Iterates through the configured worlds to find
     * active raids and analyzes them one by one.
     */
    private void getActiveRaidsInWorlds() {
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

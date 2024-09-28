package ru.ephy.raidhelper.main.raids;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.files.Config;
import ru.ephy.raidhelper.main.RaidLogic;
import ru.ephy.raidhelper.main.RaidManager;

import java.util.Map;
import java.util.logging.Logger;

/**
 * This class is responsible for scheduled checks of raids
 * in all worlds, handling certain logic in certain cases.
 */
@RequiredArgsConstructor
public class RaidScheduler {

    private final JavaPlugin plugin;        // Plugin instance
    private final RaidManager raidManager;  // RaidManager instance
    private final Config config;            // Config instance
    private final Logger logger;            // Logger for logging information

    /**
     * This method is responsible for starting the scheduler,
     * periodically running the checkRaids method.
     *
     * @throws IllegalArgumentException When an illegal argument was passed to the scheduler.
     */
    public void startScheduler() throws IllegalArgumentException {
        try {
            Bukkit.getScheduler().runTaskTimer(
                    plugin, this::checkRaids, 0, config.getFrequencyRaid());
        } catch (final IllegalArgumentException e) {
            logger.severe("An exception occured while enabling the RaidScheduler: " + e.getMessage());
        }
    }

    /**
     * This method is responsible for checking active raids
     * counters in all worlds given by the config.
     */
    private void checkRaids() {
        for (final Map.Entry<Integer, RaidLogic> entry : raidManager.getRaidMap().entrySet()) {

        }
    }
}

package ru.ephy.raidhelper.main;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;

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

    private int cooldown;

    /**
     * This method is responsible for starting the scheduler,
     * periodically running the checkRaids method.
     *
     * @throws IllegalArgumentException When an illegal argument was passed to the scheduler.
     */
    public void startScheduler() throws IllegalArgumentException {
        initializeCooldown();
        try {
            Bukkit.getScheduler().runTaskTimer(
                    plugin, this::processRaidEvents, 0, config.getFrequencyRaid());
        } catch (final IllegalArgumentException e) {
            logger.severe("An exception occured while enabling the RaidScheduler: " + e.getMessage());
        }
    }

    /**
     * Initializes the cooldown to not call it every time.
     */
    private void initializeCooldown() { cooldown = config.getCooldown(); }

    /**
     * This method is responsible for checking active raids
     * counters in all worlds given by the config.
     */
    private void processRaidEvents() {
        raidManager.getRaidMap().forEach((world, idRaidDataMap)
                -> idRaidDataMap.forEach((id, raidData) -> {
            raidData.incrementCounter();

            if (!raidData.isRingable()) {
                if (raidData.getCounter() > cooldown) {
                    raidData.setRingable(true);
                }
            }

            if (shouldBeReset(raidData.getRaid())) {
                raidData.setRingable(false);
                raidData.resetCounter();
            }
        }));
    }

    /**
     * Checks if all raiders of the raid are dead.
     *
     * @param raid The raid
     * @return True, if they are dead; false, if they are alive.
     */
    private boolean shouldBeReset(final Raid raid) { return raid.getRaiders().isEmpty(); }
}

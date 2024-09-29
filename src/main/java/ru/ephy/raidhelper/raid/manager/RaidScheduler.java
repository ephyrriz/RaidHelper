package ru.ephy.raidhelper.raid.manager;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;

import java.util.logging.Logger;

/**
 * Manages scheduled checks for active raids across all worlds.
 * Handles specific logic based on the state of the raids.
 */
@RequiredArgsConstructor
public class RaidScheduler {

    private final JavaPlugin plugin;        // Plugin's instance
    private final RaidManager raidManager;  // Manages active raids
    private final Config config;            // Holds plugin configuration settings
    private final Logger logger;            // Logger for reporting errors and information

    private int cooldownTicks;              // Cooldown duration (from config); sets when isRingable can be true

    /**
     * Starts the raid event scheduler, running periodic checks on active raids.
     *
     * @throws IllegalArgumentException If an illegal argument was passed during the scheduling process.
     */
    public void startScheduler() throws IllegalArgumentException {
        initializeCooldown();
        try {
            Bukkit.getScheduler().runTaskTimer(
                    plugin, this::checkActiveRaids, 0, config.getFrequencyRaid());
        } catch (final IllegalArgumentException e) {
            logger.severe("Error enabling RaidScheduler: " + e.getMessage());
        }
    }

    /**
     * Initializes the cooldown duration for raids.
     */
    private void initializeCooldown() {
        cooldownTicks = config.getCooldown();
    }

    /**
     * Checks the status of active raids.
     */
    private void checkActiveRaids() {
        raidManager.getRaidMap().forEach((world, idRaidDataMap) ->
                idRaidDataMap.forEach((id, raidData) -> updateRaidState(raidData)));
    }

    /**
     * Updates the ringable state, updates their counters and resets
     * counters for the given raid data.
     *
     * @param raidData The RaidData instance to update.
     */
    private void updateRaidState(final RaidData raidData) {
        raidData.incrementCounter();

        if (!raidData.isRingable()) {
            if (raidData.getCounter() > cooldownTicks) {
                raidData.setRingable(true);
            }
        } else {
            if (shouldBeReset(raidData.getRaid())) {
                raidData.setRingable(false);
                raidData.resetCounter();
            }
        }
    }

    /**
     * Checks if all raiders of the raid are dead.
     *
     * @param raid The raid to check.
     * @return True if all raiders are dead, otherwise false.
     */
    private boolean shouldBeReset(final Raid raid) {
        return raid.getRaiders().isEmpty();
    }
}

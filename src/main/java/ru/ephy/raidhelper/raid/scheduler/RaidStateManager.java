package ru.ephy.raidhelper.raid.scheduler;

import net.kyori.adventure.text.Component;
import org.bukkit.Raid;
import org.bukkit.entity.Player;
import org.bukkit.entity.Raider;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages the state of raids, handling the logic for wave progression
 * and player notifications based on raid status.
 */
public class RaidStateManager {

    private final Logger logger;       // Logger for debugging

    private final Component message;   // Action bar message for players
    private final double radius;       // Radius for notifying players
    private final int cooldownTicks;   // Cooldown duration

    /**
     * Initializes the RaidStateManager with configuration values.
     * Sets up the action bar message, notification radius, and cooldown ticks.
     */
    public RaidStateManager(final Config config, final Logger logger) {
        this.logger = logger;

        message = config.getRingMessage();
        radius = config.getRadius();
        cooldownTicks = config.getBellWorkDelay();
    }

    /**
     * Updates the state of the given raid based on its current status.
     *
     * @param raidData The RaidData instance containing information about the current raid.
     */
    public void updateRaidState(final RaidData raidData) {
        final Raid raid = raidData.getRaid();

        // Determine the status of the raid and handle accordingly.
        if (isWaveEnded(raid)) {
            handleWaveEnd(raidData); // Handle logic for ended wave.
        } else {
            handleOngoingWave(raidData); // Handle logic for ongoing wave.
        }

        logger.info("\nDEBUG: " + raidData.toString());
    }

    /**
     * Checks if all raiders of the raid are dead, indicating the wave has ended.
     *
     * @param raid The raid to check for active raiders.
     * @return True if all raiders are dead, otherwise false.
     */
    private boolean isWaveEnded(final Raid raid) {
        final List<Raider> raiderList = raid.getRaiders();
        return raiderList.isEmpty();
    }

    /**
     * Handles the logic when a wave has ended, resetting necessary flags and counters.
     *
     * @param raidData The RaidData instance to update when the wave ends.
     */
    private void handleWaveEnd(final RaidData raidData) {
        if (!raidData.isCanResetCounter()) { // Checks if the flag is already false; protects from multiple calls.
            raidData.setCanResetCounter(true);
            raidData.setRingable(false);
            raidData.resetCounter();
        }
    }

    /**
     * Handles the logic for an ongoing wave, updating the raidData state
     * based on the current counter and cooldown.
     *
     * @param raidData The RaidData instance to update during the ongoing wave.
     */
    private void handleOngoingWave(final RaidData raidData) {
        raidData.setCanResetCounter(false); // Reset flag indicating the wave is ongoing.

        if (raidData.getCounter() > cooldownTicks) {  // Check if cooldown is exceeded.
            handleRingableState(raidData);
        } else {
            raidData.incrementCounter(); // Increment the counter if below cooldown.
        }
    }

    /**
     * Handles the ringable state of the raidData, including sending action bar messages
     * to nearby players when applicable.
     *
     * @param raidData The RaidData instance to check and update the ringable state.
     */
    private void handleRingableState(final RaidData raidData) {
        if (!raidData.isRingable()) {
            raidData.setRingable(true); // Set to ringable if not already set
        }

        if (raidData.isRingable()) {
            sendActionBarMessage(raidData); // Notify players
        }
    }

    /**
     * Notifies players in the vicinity via an action bar message.
     * Retrieves nearby players within the specified radius and sends them the action bar message.
     *
     * @param raidData The RaidData instance that contains location information for sending messages.
     */
    private void sendActionBarMessage(final RaidData raidData) {
        final Collection<Player> nearbyPlayers = raidData.getLocation().getNearbyPlayers(radius);
        nearbyPlayers.forEach(player -> player.sendActionBar(message));
    }
}

package ru.ephy.raidhelper.raid.scheduler;

import net.kyori.adventure.text.Component;
import org.bukkit.Raid;
import org.bukkit.entity.Player;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * Manages raid states and handles player notifications.
 */
public class RaidStateManager {

    private final Logger logger;             // Logger for debugging

    private final Component message;         // Message sent to players
    private final double notificationRadius; // Radius to send messages to players
    private final int cooldownTicks;         // Cooldown duration in ticks

    /**
     * Constructs the {@link RaidStateManager} for updating raid data states.
     *
     * @param config Configuration for raid behavior
     * @param logger Logger for debug information
     */
    public RaidStateManager(final Config config, final Logger logger) {
        this.logger = logger;

        message = config.getRingMessage();
        notificationRadius = config.getRadius();
        cooldownTicks = config.getBellWorkDelay();
    }

    /**
     * Updates the state of the given raid.
     *
     * @param raidData RaidData instance with the raid's current status
     */
    public void updateRaidState(final RaidData raidData) {
        if (isWaveEnded(raidData.getRaid())) {
            logger.info("DEBUG-A: Wave is ended. Handling the Wave End.");
            handleWaveEnd(raidData);
        } else {
            logger.info("DEBUG-A: Wave is not ended. Handling the Wave Ongoing.");
            handleOngoingWave(raidData);
        }
        logger.info("DEBUG-E: RaidData - " + raidData.toString());
    }

    /**
     * Checks if all raiders are defeated, signaling
     * the end of a wave.
     *
     * @param raid The raid to check
     * @return True if all raiders are defeated, false otherwise
     */
    private boolean isWaveEnded(final Raid raid) {
        return raid.getRaiders().isEmpty();
    }

    /**
     * Handles the end of a wave by resetting flags
     * and counters.
     *
     * @param raidData RaidData instance to update
     */
    private void handleWaveEnd(final RaidData raidData) {
        if (!raidData.isCanResetCounter()) {
            logger.info("DEBUG-B: The flag can reset is false for " + raidData.toString() + ". Modifying values.");
            raidData.setCanResetCounter(true);
            raidData.setCanTeleport(false);
            raidData.resetCounter();
            logger.info("DEBUG-B: Modification has ended. New values - " + raidData.toString());
            return;
        }
        logger.info("DEBUG-B: The flag can reset is true for " + raidData.toString());
    }

    /**
     * Updates the raid's state during an ongoing wave,
     * managing cooldown and notifications.
     *
     * @param raidData RaidData instance to update
     */
    private void handleOngoingWave(final RaidData raidData) {
        if (raidData.isCanResetCounter()) {
            logger.info("DEBUG-C: The flag can reset is true for " + raidData.toString() + ". Modifying values.");
            raidData.setCanResetCounter(false);
            logger.info("DEBUG-C: Modification has ended. New values - " + raidData.toString());
        } else {
            logger.info("DEBUG-C: The flag can reset is false for " + raidData.toString());
        }

        if (raidData.getCounter() > cooldownTicks) {
            logger.info("DEBUG-C: The counter is above the cooldownTicks for " + raidData.toString() + ". Modifying values.");
            updateRingableState(raidData);
            logger.info("DEBUG-C: Modification has ended. New values - " + raidData.toString());
        } else {
            logger.info("DEBUG-C: The counter is below the cooldownTicks for " + raidData.toString() + ". Modifying values.");
            raidData.incrementCounter();
            logger.info("DEBUG-C: Modification has ended. New values - " + raidData.toString());
        }
    }

    /**
     * Updates the ringable state and notifies players if necessary.
     *
     * @param raidData RaidData to update
     */
    private void updateRingableState(final RaidData raidData) {
        if (!raidData.isCanTeleport()) {
            logger.info("DEBUG-D: The flag can teleport is false for " + raidData.toString() + ". Modifying values.");
            raidData.setCanTeleport(true);
            logger.info("DEBUG-D: Modification has ended. New values - " + raidData.toString());
        } else {
            logger.info("DEBUG-D: The flag can teleport is true for " + raidData.toString());
        }
        sendActionBarMessage(raidData);
        logger.info("DEBUG-D: Sending messages to players.");
    }

    /**
     * Sends a notification message to players nearby
     * the raid's location.
     *
     * @param raidData RaidData with location information
     */
    private void sendActionBarMessage(final RaidData raidData) {
        final Collection<Player> nearbyPlayers = raidData.getLocation().getNearbyPlayers(notificationRadius);

        for (final Player player : nearbyPlayers) {
            player.sendMessage(message);
        }
    }
}

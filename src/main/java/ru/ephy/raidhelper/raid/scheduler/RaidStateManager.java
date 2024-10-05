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
            handleWaveEnd(raidData);
        } else {
            handleOngoingWave(raidData);
        }
        logger.info("\nDEBUG: " + raidData.toString());
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
            raidData.setCanResetCounter(true);
            raidData.setRingable(false);
            raidData.resetCounter();
        }
    }

    /**
     * Updates the raid's state during an ongoing wave,
     * managing cooldown and notifications.
     *
     * @param raidData RaidData instance to update
     */
    private void handleOngoingWave(final RaidData raidData) {
        if (raidData.isCanResetCounter()) {
            raidData.setCanResetCounter(false);
        }

        if (raidData.getCounter() > cooldownTicks) {
            updateRingableState(raidData);
        } else {
            raidData.incrementCounter();
        }
    }

    /**
     * Updates the ringable state and notifies players if necessary.
     *
     * @param raidData RaidData to update
     */
    private void updateRingableState(final RaidData raidData) {
        if (!raidData.isRingable()) {
            raidData.setRingable(true);
        }
        sendActionBarMessage(raidData);
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

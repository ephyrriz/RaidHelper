package ru.ephy.raidhelper.raid.scheduler;

import net.kyori.adventure.text.Component;
import org.bukkit.Raid;
import org.bukkit.entity.Player;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * Handles the raid state updates and player
 * notifications during a raid.
 */
public class RaidStateManager {

    private final Logger logger;               // Logger for debugging

    private final Component notificationMessage;  // Message sent to nearby players
    private final double notifyRadius;         // Radius for player notifications
    private final int cooldownDuration;        // Cooldown between updates in ticks

    /**
     * Initializes the RaidStateManager with configuration settings.
     *
     * @param config Configuration for raid notifications and behavior
     * @param logger Logger for debugging and info logging
     */
    public RaidStateManager(final Config config, final Logger logger) {
        // Initializes required instance
        this.logger = logger;

        // Initializes required variables
        notificationMessage = config.getRingMessage();
        notifyRadius = config.getRadius();
        cooldownDuration = config.getBellWorkDelay();
    }

    /**
     * Updates the state of the given raid, either
     * handling wave end or processing ongoing waves.
     *
     * @param raidData Data associated with the current raid state
     */
    public void updateRaidState(final RaidData raidData) {
        if (hasWaveEnded(raidData.getRaidInstance())) {
            processWaveEnd(raidData);
        } else {
            processOngoingWave(raidData);
        }
    }

    /**
     * Checks if the current raid wave has ended (all raiders defeated).
     *
     * @param raid The raid instance to check
     * @return True if the current wave has ended, false otherwise
     */
    private boolean hasWaveEnded(final Raid raid) {
        logger.fine("Raid #" + raid.getId() + ": total health = " + raid.getTotalHealth() + ". Is empty? " + raid.getRaiders().isEmpty());
        return raid.getRaiders().isEmpty();
    }

    /**
     * Handles the end of a raid wave by resetting certain flags
     * and counters in the raid data.
     *
     * @param raidData The raid data to update after a wave ends
     */
    private void processWaveEnd(final RaidData raidData) {
        if (raidData.isCounterResetAllowed()) {
            raidData.setCounterResetAllowed(false);
            raidData.setTeleportEnabled(false);
            raidData.resetCounter();
        }
    }

    /**
     * Processes an ongoing raid wave by managing
     * cooldowns and triggering actions when needed.
     *
     * @param raidData The raid data to update for ongoing waves
     */
    private void processOngoingWave(final RaidData raidData) {
        if (!raidData.isCounterResetAllowed()) {
            raidData.setCounterResetAllowed(true);
        }

        if (raidData.getTickCounter() > cooldownDuration) {
            enableTeleportAndNotify(raidData);
        } else {
            raidData.incrementCounter();
        }
    }

    /**
     * Enables teleporting and sends notifications to nearby players.
     *
     * @param raidData The raid data to update with notifications
     */
    private void enableTeleportAndNotify(final RaidData raidData) {
        if (!raidData.isTeleportEnabled()) {
            raidData.setTeleportEnabled(true);
        }
        notifyNearbyPlayers(raidData);
    }

    /**
     * Notifies nearby players with an action bar message.
     *
     * @param raidData The raid data containing the location and radius for notifications
     */
    private void notifyNearbyPlayers(final RaidData raidData) {
        final Collection<Player> nearbyPlayers = raidData.getRaidLocation().getNearbyPlayers(notifyRadius);

        for (final Player player : nearbyPlayers) {
            player.sendActionBar(notificationMessage);
        }
    }
}

package ru.ephy.raidhelper.raids;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.files.Config;

import java.util.logging.Logger;

/**
 * Manages logic related to a specific raid event.
 * It handles the scheduling of tasks, updating a timer,
 * and interacting with raid-related events like the bell.
 */
@Getter
@RequiredArgsConstructor
public class RaidLogic {

    private final JavaPlugin plugin;       // Plugin instance reference
    private final Config config;           // Holds configuration data
    private final Raid raid;               // Raid instance
    private final Logger logger;           // Logger for logging information

    private boolean isBellActive = false;  // Indicates if the bell can be used for teleporting raiders
    private long raidCounter = 0;          // Counter tracking the raid's progress
    private int counterTaskId = -1;        // ID of the scheduled task for this raid logic

    /**
     * Starts the scheduled task that updates the raid's counter.
     * The task will run at a fixed interval defined in the config.
     *
     * @throws IllegalArgumentException throws in case if an inappropriate argument was passed
     */
    public void startRaidCounter() throws IllegalArgumentException {
        try {
            counterTaskId = Bukkit.getScheduler().runTaskTimer(
                    plugin, () -> {
                        if (!isRaidersRemaining()) {
                            resetRaidCounter();
                        }
                        updateRaidCounter();
                        notifyPlayers();
                    }, 0, 20).getTaskId();
        } catch (final Exception e) {
            logger.severe("An error occured during starting the scheduler for the raid: " + raid + ". Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the raid counter and checks if the bell is ready to teleport raiders.
     * The bell becomes active when the counter exceeds the cooldown defined in the config.
     */
    private void updateRaidCounter() {
        if (!isBellActive) {
            raidCounter += 1;
            if (raidCounter > config.getCooldown()) {
                isBellActive = true;
            }
        }
    }

    /**
     * Checks for remaining raiders of the raid.
     */
    private boolean isRaidersRemaining() {
        return !raid.getRaiders().isEmpty();
    }

    /**
     * Sends an action bar message to players within the raid's radius when the bell is ready.
     */
    private void notifyPlayers() {
        if (isBellActive) {
            for (final Player player : raid.getLocation().getNearbyPlayers(config.getRadius())) {
                player.sendActionBar(Component.text(config.getMessage()));
            }
        }
    }

    /**
     * Resets the raid counter and marks the bell as inactive.
     */
    private void resetRaidCounter() {
        raidCounter = 0;
        isBellActive = false;
    }

    /**
     * Stops the scheduled raid counter task.
     */
    public void stopRaidCounter() {
        if (counterTaskId != -1) {
            logger.warning("stopRaidCounter: Condition has passed for stopCounter. Raid: " + raid.getId() + ". taskId: " + counterTaskId);
            Bukkit.getScheduler().cancelTask(counterTaskId);
            counterTaskId = -1;
            return;
        }
        logger.warning("stopRaidCounter: Condition hasn't passed for stopCounter. Raid: " + raid.getId() + ". taskId: " + counterTaskId);
    }
}

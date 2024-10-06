package ru.ephy.raidhelper.raid.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;

/**
 * Represents data associated with a specific raid,
 * including its location, world, and raider behavior settings.
 * Tracks a time counter which can be incremented or reset.
 */
@Getter
@NonNull
@RequiredArgsConstructor
public class RaidData {

    private final int raidId;                       // Unique identifier for the raid
    private final Raid raidInstance;                // Reference to the Raid instance
    private final Location raidLocation;            // Location where the raid is occurring
    private final World raidWorld;                  // World in which the raid takes place

    @Setter
    private boolean teleportEnabled = false;      // Allows raiders to teleport when the bell rings
    @Setter
    private boolean cooldownActive = false;       // Indicates if the raid is in cooldown
    @Setter
    private boolean counterResetAllowed = false;  // Prevents counter reset if false
    private int tickCounter = 0;                    // Tracks time (in ticks) since the raid started or was reset

    /**
     * Increments the tick counter by one.
     */
    public void incrementCounter() {
        tickCounter++;
    }

    /**
     * Resets the tick counter to zero.
     */
    public void resetCounter() {
        tickCounter = 0;
    }

    /**
     * Provides a string representation of RaidData for debugging.
     *
     * @return A string with the values of RaidData.
     */
    @Override
    public String toString() {
        return "RaidData{" +
                "id=" + raidId +
                ", raidInstance=" + raidInstance +
                ", raidLocation=" + raidLocation +
                ", raidWorld=" + raidWorld +
                ", isTeleportEnabled=" + teleportEnabled +
                ", isCooldownActive=" + cooldownActive +
                ", isCounterResetAllowed=" + counterResetAllowed +
                ", tickCounter=" + tickCounter +
                '}';
    }
}

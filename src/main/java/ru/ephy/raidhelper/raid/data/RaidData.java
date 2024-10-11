package ru.ephy.raidhelper.raid.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Raider;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents data associated with a specific raid,
 * including its location, world, and raider behavior settings.
 * Tracks a time counter which can be incremented or reset.
 */
@Getter
@Setter
@NonNull
@RequiredArgsConstructor
public class RaidData {

    private final int raidId;                       // Unique identifier for the raid
    private final Raid raidInstance;                // Reference to the Raid instance
    private final Location raidLocation;            // Location where the raid is occurring
    private final World raidWorld;                  // World in which the raid takes place

    private Set<Player> playersWithinRaid;          // Set of players within the raid's range
    private Set<Raider> raiderSet;                 // Set of raiders of the raid
    private boolean teleportEnabled = false;        // Allows raiders to teleport when the bell rings
    private boolean cooldownActive = false;         // Indicates if the raid is in cooldown
    private boolean counterResetAllowed = false;    // Prevents counter reset if false
    private AtomicLong lastUpdatedTime;             // Last updated time of the cache
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
                "raidId=" + raidId +
                ", raidInstance=" + raidInstance +
                ", raidLocation=" + raidLocation +
                ", raidWorld=" + raidWorld +
                ", playersWithinRaid=" + playersWithinRaid +
                ", raiderSet" + raiderSet +
                ", isTeleportEnabled=" + teleportEnabled +
                ", isCooldownActive=" + cooldownActive +
                ", isCounterResetAllowed=" + counterResetAllowed +
                ", lastUpdatedTime=" + lastUpdatedTime +
                ", tickCounter=" + tickCounter +
                '}';
    }
}

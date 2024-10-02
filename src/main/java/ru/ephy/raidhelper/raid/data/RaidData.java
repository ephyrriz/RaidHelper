package ru.ephy.raidhelper.raid.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;

/**
 * Holds data related to a specific raid, including the raid's location,
 * the world it takes place in, and settings related to raider behavior
 * (e.g., teleportation when the bell rings). This class also tracks time
 * via a counter, which can be reset or incremented.
 */
@Getter
@NonNull
@RequiredArgsConstructor
public class RaidData {

    private final int raidId;                // The associated Raid's id
    private final Raid raid;                 // The associated Raid instance
    private final Location location;         // The location where the raid is occurring
    private final World world;               // The world in which the raid is taking place

    @Setter
    private boolean ringable = false;        // Determines if raiders can teleport when the bell rings
    @Setter
    private boolean inCooldown = false;      // If true, the raid is in cooldown and won't teleport raiders
    @Setter
    private boolean canResetCounter = false; // If true, prevents the counter from being reset
    private int counter = 0;                 // Tracks time (in ticks) since the raid was initialized or reset

    /**
     * Increments the counter by 1.
     */
    public void incrementCounter() {
        counter++;
    }

    /**
     * Resets the counter to 0.
     */
    public void resetCounter() {
        counter = 0;
    }

    /**
     * For debug purposes only.
     *
     * @return Returns all values of the RaidData.
     */
    @Override
    public String toString() {
        return "\nRaidData{" +
                "raidId=" + raidId +
                ", raid=" + raid +
                ", location=" + location +
                ", world=" + world +
                ", ringable=" + ringable +
                ", inCooldown=" + inCooldown +
                ", canResetCounter=" + canResetCounter +
                ", counter=" + counter +
                '}';
    }
}

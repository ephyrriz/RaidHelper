package ru.ephy.raidhelper.raid.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;

/**
 * Represents data related to a specific raid event, storing
 * its location, world, and whether raiders can be teleported
 * when the bell rings. Also tracks the time elapsed via a counter.
 */
@Getter
@NonNull
@RequiredArgsConstructor
public class RaidData {
    private final Raid raid;                      // Associated Raid instance
    private final Location location;              // Location of the raid
    private final World world;                    // World where the raid is taking place

    @Setter
    private boolean ringable = false;             // If true, raiders can teleport on bell ring
    @Setter
    private boolean reset = false;
    private int counter = 0;                      // Tracks the time since the object was created

    public void incrementCounter() { counter++; } // Increments the counter by 1.
    public void resetCounter() { counter = 0; }   // Resets the counter to 0.
}

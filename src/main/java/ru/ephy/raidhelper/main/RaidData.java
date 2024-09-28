package ru.ephy.raidhelper.main;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;

/**
 * Manages logic related to a specific raid event.
 * It handles the scheduling of tasks, updating a timer,
 * and interacting with raid-related events like the bell.
 */
@Getter
@RequiredArgsConstructor
public class RaidData {
    private final Raid raid;                      // Raid instance
    private final Location location;              // Location instance
    private final World world;                    // World instance

    @Setter
    private boolean isRingable = false;           // If true, ring in a bell will teleport raiders; if false, no logic.
    private int counter = 0;                      // Counter; is used to measure the time since the object was created.

    public void incrementCounter() { counter++; } // Increments the counter by 1.
    public void resetCounter() { counter = 0; }   // Resets the counter to 0.
}

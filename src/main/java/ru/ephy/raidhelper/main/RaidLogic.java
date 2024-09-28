package ru.ephy.raidhelper.main;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Raid;
import org.bukkit.World;
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
    private final JavaPlugin plugin;  // Plugin instance reference
    private final Config config;      // Holds configuration data
    private final Raid raid;          // Raid instance
    private final World world;        // World instance
    private final Logger logger;      // Logger for logging information
    private boolean isRingable;       // If true, ring in a bell will teleport raiders; if false, no logic.
}

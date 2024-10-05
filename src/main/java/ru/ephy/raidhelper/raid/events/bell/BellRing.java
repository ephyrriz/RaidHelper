package ru.ephy.raidhelper.raid.events.bell;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.*;
import java.util.logging.Logger;

/**
 * Handles the event of a bell being rung during a raid.
 * It validates whether the bell is located in a configured world
 * and triggers the teleportation of raiders if conditions are met.
 */
public class BellRing implements Listener {

    private final JavaPlugin plugin;                      // Reference to the plugin instance
    private final Config config;                          // Configuration settings for raid handling
    private final RaidManager raidManager;                // Manages active raids
    private final Logger logger;                          // Logger for debugging and information

    private final TeleporterPool teleporterPool;          // Pool of reusable Teleporter instances
    private final Map<Location, Boolean> locationBellMap; // Location bell mao
    private final Set<World> allowedWorlds;               // Set of worlds where raid events are allowed

    /**
     * Constructs a BellRingListener to handle bell ring events during raids.
     *
     * @param plugin       The plugin instance used for scheduling tasks and managing events
     * @param raidManager  Manager that tracks and processes raid data
     * @param config       Configuration object containing raid-related settings
     * @param logger       Logger for recording events and errors
     */
    public BellRing(final JavaPlugin plugin, final RaidManager raidManager,
                    final Config config, final Logger logger) {
        this.plugin = plugin;
        this.config = config;
        this.raidManager = raidManager;
        this.logger = logger;

        allowedWorlds = config.getWorldSet();
        locationBellMap = new WeakHashMap<>();

        teleporterPool = new TeleporterPool(config);
    }

    /**
     * Handles the bell ring event. If the bell is located in a valid world,
     * it triggers the teleportation of raiders within range.
     *
     * @param event The bell ring event triggered by a player
     */
    @EventHandler
    public void on(final BellRingEvent event) {
        if (event.getEntity() instanceof final Player player) {
            final Location bellLocation = event.getBlock().getLocation();
            final World bellWorld = bellLocation.getWorld();

            if (isBellLocationCachedOrAllowed(bellLocation, bellWorld)) {
                handleTeleport(player, bellWorld, bellLocation);
            }
        }
    }

    /**
     * Handles the teleportation logic by borrowing a Teleporter from the pool
     * and initiating the teleportation process.
     *
     * @param player       The player who rang the bell
     * @param bellLocation The location of the bell
     */
    private void handleTeleport(final Player player, final World bellWorld, final Location bellLocation) {
        final Teleporter teleporter = teleporterPool.borrowTeleporter(plugin, raidManager, config, logger);

        teleporter.handleTeleport(player, bellWorld, bellLocation);
    }

    /**
     * Checks whether the given world is in the set of worlds where raids are allowed.
     *
     * @param bellWorld The world to check
     * @return true if the world is in the configured set, false otherwise
     */
    private boolean isBellLocationCachedOrAllowed(final Location bellLocation, final World bellWorld) {
        return locationBellMap.computeIfAbsent(bellLocation, location ->
                allowedWorlds.contains(bellWorld));
    }
}

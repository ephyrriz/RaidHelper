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
 * Handles bell ring events during a raid. If the bell
 * is located in a configured world, it triggers the
 * teleportation of raiders if conditions are met.
 */
public class BellRing implements Listener {

    private final JavaPlugin plugin;                // Plugin reference
    private final Config config;                    // Configuration settings
    private final RaidManager raidManager;          // Manages active raids
    private final Logger logger;                    // Logger for debugging

    private final TeleporterPool teleportPool;      // Pool for reusable Teleporter instances
    private final Map<Location, Boolean> bellCache; // Cached bell locations
    private final Set<World> validWorlds;           // Worlds where raid events are valid

    /**
     * Constructs a BellRing handler for managing bell ring events during raids.
     *
     * @param plugin       Plugin instance for scheduling and managing tasks
     * @param raidManager  Manages raid data and processes active raids
     * @param config       Contains settings related to raids
     * @param logger       Logs events and errors
     */
    public BellRing(final JavaPlugin plugin, final RaidManager raidManager,
                    final Config config, final Logger logger) {
        // Initializes required instances
        this.plugin = plugin;
        this.config = config;
        this.raidManager = raidManager;
        this.logger = logger;

        // Initializes required variables
        validWorlds = config.getValidWorlds();

        bellCache = new WeakHashMap<>();
        teleportPool = new TeleporterPool(config);
    }

    /**
     * Handles the bell ring event. If the bell is in a valid world,
     * triggers teleportation of raiders nearby.
     *
     * @param event Bell ring event triggered by a player
     */
    @EventHandler
    public void on(final BellRingEvent event) {
        if (event.getEntity() instanceof final Player player) {
            final Location bellLocation = event.getBlock().getLocation();
            final World bellWorld = bellLocation.getWorld();

            if (isValidBellLocation(bellLocation, bellWorld)) {
                processTeleport(player, bellWorld, bellLocation);
            }
        }
    }

    /**
     * Handles teleportation by borrowing a Teleporter from the pool and
     * initiating the teleport process.
     *
     * @param player       Player who rang the bell
     * @param bellWorld    The world where the bell is located
     * @param bellLocation Location of the bell
     */
    private void processTeleport(final Player player, final World bellWorld, final Location bellLocation) {
        final Teleporter teleporter = teleportPool.getTeleporter(plugin, raidManager, config, logger);

        teleporter.handleTeleport(player, bellWorld, bellLocation);
    }

    /**
     * Checks if the bell is in a valid world or if the location is cached.
     *
     * @param bellLocation Bell's location
     * @param bellWorld    World where the bell is located
     * @return true if the bell location is valid, false otherwise
     */
    private boolean isValidBellLocation(final Location bellLocation, final World bellWorld) {
        return bellCache.computeIfAbsent(bellLocation, location -> validWorlds.contains(bellWorld));
    }
}

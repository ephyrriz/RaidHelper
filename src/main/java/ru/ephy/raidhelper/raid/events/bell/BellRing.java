package ru.ephy.raidhelper.raid.events.bell;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Bell;
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
 * Listens to bell rings, verifying them for the
 * world location, passing it to the teleporter
 * class if this bell is within the one of the
 * configured worlds.
 */
public class BellRing implements Listener {

    private final JavaPlugin plugin;                    // Plugin's instance
    private final Config config;                        // Config's instance; holds configured data
    private final RaidManager raidManager;              // Manages the raids map
    private final Logger logger;

    private final TeleporterPool teleporterPool;        // Pool of Teleporter class
    private final Set<World> worldSet;                  // Set of worlds from the config
    private final Map<Location, Bell> bellLocationMap;  // Map of locations with bell assigned to them

    /**
     * Constructs the {@link BellRing} class.
     *
     * @param raidManager  The RaidManager to use when creating instances of teleporter
     * @param config       The Config's instance to initialize needed variables
     */
    public BellRing(final JavaPlugin plugin, final RaidManager raidManager,
                    final Config config, final Logger logger) {
        this.plugin = plugin;
        this.config = config;
        this.raidManager = raidManager;
        this.logger = logger;
        worldSet = config.getWorldSet();
        bellLocationMap = new HashMap<>();
        teleporterPool = new TeleporterPool(config);
    }

    /**
     * Listents to the {@link BellRingEvent}.
     * If the event fires, it verifies the Bell's location.
     * If the bell's location is within the set of processing
     * worlds, it's being put in the WeakHashMap in case of
     * multiple calls.
     *
     * @param event BellRingEvent
     */
    @EventHandler
    public void on(final BellRingEvent event) {
        if (event.getEntity() instanceof final Player player) {
            final Bell bell = (Bell) event.getBlock();
            final Location bellLocation = bell.getLocation();
            final World bellWorld = bellLocation.getWorld();

            if (isWorldInConfig(bellWorld) || bellLocationMap.containsKey(bellLocation)) {
                bellLocationMap.putIfAbsent(bellLocation, bell);
                final Teleporter teleporter = teleporterPool.borrowTeleporter(plugin, raidManager, config, logger);
                teleporter.handleTeleport(player, bellWorld, bellLocation);
            }
        }
    }

    /**
     * This method verifies the passed world on its
     * excistence in the worldSet. If it exists,
     * it means that the world is in the set of
     * worlds loaded by the config -- the ones that
     * we work with.
     *
     * @param world Bell's location world
     * @return true if the world is in the set. Otherwise, returns false
     */
    private boolean isWorldInConfig(final World world) {
        return worldSet.contains(world);
    }
}

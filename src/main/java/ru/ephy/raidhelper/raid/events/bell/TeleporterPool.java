package ru.ephy.raidhelper.raid.events.bell;

import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Manages a pool of reusable {@link Teleporter} instances to
 * optimize performance by reusing objects during raid
 * teleportation, reducing creation overhead.
 */
public class TeleporterPool {

    private final Queue<Teleporter> teleporterQueue;  // Queue holding available Teleporters
    private final int maxPoolSize;                    // Max number of Teleporters in the pool

    /**
     * Initializes the TeleporterPool to manage reusable Teleporter instances.
     *
     * @param config Configuration that defines pool size.
     */
    public TeleporterPool(final Config config) {
        teleporterQueue = new LinkedList<>();
        maxPoolSize = config.getMaxPoolSize();
    }

    /**
     * Provides an available Teleporter instance or creates a new one if the pool is empty.
     *
     * @param plugin       The plugin instance required by the teleporter.
     * @param raidManager  The RaidManager handling raid logic.
     * @param config       Config instance for initializing teleport variables.
     * @param logger       Logger for logging debug or informational messages.
     * @return A Teleporter instance from the pool or a newly created one.
     */
    public Teleporter getTeleporter(final JavaPlugin plugin, final RaidManager raidManager,
                                    final Config config, final Logger logger) {
        if (!teleporterQueue.isEmpty()) {
            return teleporterQueue.poll();
        }
        return new Teleporter(plugin, this, raidManager, config, logger);
    }

    /**
     * Returns the used Teleporter instance back to the pool for future use.
     *
     * @param teleporter The Teleporter instance to return.
     */
    public void returnTeleporter(final Teleporter teleporter) {
        if (teleporterQueue.size() < maxPoolSize) {
            teleporterQueue.offer(teleporter);
        }
    }
}

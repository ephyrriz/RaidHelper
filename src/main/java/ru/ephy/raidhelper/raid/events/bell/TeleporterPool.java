package ru.ephy.raidhelper.raid.events.bell;

import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * This class manages the pool of {@link Teleporter}
 * instances to reduce the load on the server during
 * periodic calls of the {@link BellRing} class.
 */
public class TeleporterPool {

    private final Queue<Teleporter> pool;
    private final int poolMaxSize;

    /**
     * Constructs the {@link TeleporterPool} for managing Teleport instances
     *
     * @param config Configuration
     */
    public TeleporterPool(final Config config) {
        pool = new LinkedList<>();
        poolMaxSize = config.getPoolMaxSize();
    }

    /**
     * Lets the {@link BellRing} borrow one of
     * {@link Teleporter} to handle the teleport process.
     *
     * @param plugin       The Plugin's instance for schedulers
     * @param raidManager  The RaidManager to use for processing raids
     * @param config       The Config's instance for initializing needed variables
     * @param logger       Logger for logging messages
     * @return one of teleporters' instances
     */
    public Teleporter borrowTeleporter(final JavaPlugin plugin, final RaidManager raidManager,
                                       final Config config, final Logger logger) {
        if (!pool.isEmpty()) {
            return pool.poll();
        }
        return new Teleporter(plugin, raidManager, config, logger);
    }

    /**
     * Releases the used {@link Teleporter} back in the pool.
     *
     * @param teleporter The used Teleporter
     */
    public void releaseHandler(final Teleporter teleporter) {
        if (pool.size() < poolMaxSize) {
            pool.offer(teleporter);
        }
    }
}

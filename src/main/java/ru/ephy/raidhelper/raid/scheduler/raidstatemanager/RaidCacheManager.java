package ru.ephy.raidhelper.raid.scheduler.raidstatemanager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages the caching of RaidData objects, ensuring
 * efficient updates to nearby players and raiders
 * based on configurable expiration times.
 */
public class RaidCacheManager {

    private final JavaPlugin plugin;

    private final ConcurrentLinkedQueue<RaidData> cache;
    private final double notifyRadius;
    private final int cacheExpirationTime;
    private final int batchSize;
    private int taskId = -1;

    /**
     * Initializes the RaidCacheManager with the plugin
     * instance and config values.
     *
     * @param plugin The plugin instance
     * @param config Configuration object for cache settings
     */
    public RaidCacheManager(final JavaPlugin plugin, final Config config) {
        // Initializes required instances
        this.plugin = plugin;

        // Initializes required variables
        notifyRadius = config.getRadius();
        cacheExpirationTime = config.getCacheExpireTime();
        batchSize = config.getMaxChecksPerTick();

        cache = new ConcurrentLinkedQueue<>();

        // Starts the scheduler
        startCacheScheduler();
    }

    /**
     * Adds RaidData to the cache for processing.
     *
     * @param raidData The RaidData object to add to the cache
     */
    public void addRaidData(final RaidData raidData) {
        cache.offer(raidData);
    }

    /**
     * Starts an asynchronous task that processes
     * RaidData objects in batches. Updates cache
     * if necessary based on expiration time.
     */
    private void startCacheScheduler() {
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!cache.isEmpty()) {
                int processedCount = 0;

                while (processedCount < batchSize && !cache.isEmpty()) {
                    final RaidData raidData = cache.poll();

                    if (raidData != null && doWeNeedToUpdateCache(raidData)) {
                        updateCacheIfNeeded(raidData);
                    }

                    processedCount++;
                }
            } else {
                if (Bukkit.getScheduler().isCurrentlyRunning(taskId)) {
                    Bukkit.getScheduler().cancelTask(taskId);
                }
            }
        }, 0L, 1L).getTaskId();
    }

    /**
     * Checks if the cache for a given RaidData needs to be updated.
     *
     * @param raidData The RaidData to check
     * @return true if the cache should be updated, false otherwise
     */
    private boolean doWeNeedToUpdateCache(final RaidData raidData) {
        final long currentTime = System.currentTimeMillis() / 50; // We get current time in ticks. 1 tick has 50ms
        final long lastUpdatedTime = raidData.getLastUpdatedTime();

        if (currentTime - lastUpdatedTime > cacheExpirationTime) { // We compare the amount of time passed
            raidData.setLastUpdatedTime(currentTime); // Updates the last updated time if the cache should be updated
            return true;
        }
        return false;
    }

    /**
     * Updates the cache for a given RaidData by
     * fetching nearby players and raiders.
     *
     * @param raidData The RaidData whose cache needs to be updated
     */
    public void updateCacheIfNeeded(final RaidData raidData) {
        Bukkit.getScheduler().runTask(plugin, () -> { // We cache new values here
            raidData.setPlayersWithinRaid(new HashSet<>(raidData.getRaidLocation().getNearbyPlayers(notifyRadius)));
            raidData.setRaiderSet(new HashSet<>(raidData.getRaidInstance().getRaiders()));
        });
    }
}

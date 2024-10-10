package ru.ephy.raidhelper.raid.scheduler.raidstatemanager;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RaidCacheManager {

    private final JavaPlugin plugin;

    private final ConcurrentLinkedQueue<RaidData> cache;
    private final double notifyRadius;
    private final int cacheExpirationTime;
    private final int batchSize;

    public RaidCacheManager(final JavaPlugin plugin, final Config config) {
        // Initializes required instances
        this.plugin = plugin;

        // Initializes required variables
        notifyRadius = Math.pow(config.getRadius(), 2);
        cacheExpirationTime = config.getCacheExpireTime();
        batchSize = config.getMaxChecksPerTick();

        cache = new ConcurrentLinkedQueue<>();

        // Starts the scheduler
        startCacheScheduler();
    }

    public void addRaidData(final RaidData raidData) {
        cache.offer(raidData);
    }

    private void startCacheScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!cache.isEmpty()) {
                    int processedCount = 0;

                    while (processedCount < batchSize && !cache.isEmpty()) {
                        final RaidData raidData = cache.poll();
                        if (raidData != null) {
                            updateCacheIfNeeded(raidData);
                            cache.remove(raidData);
                            processedCount++;
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L);
    }

    public void updateCacheIfNeeded(final RaidData raidData) {
        final long currentTime = System.currentTimeMillis() / 50;
        final long lastUpdatedTime = raidData.getLastUpdatedTime();

        if (currentTime - lastUpdatedTime > cacheExpirationTime) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    raidData.setPlayersWithinRaid(new HashSet<>(raidData.getRaidLocation().getNearbyPlayers(notifyRadius)));
                    raidData.setRaiderSet(new HashSet<>(raidData.getRaidInstance().getRaiders()));
                    raidData.setLastUpdatedTime(currentTime);
                }
            }.runTask(plugin);
        }
    }
}

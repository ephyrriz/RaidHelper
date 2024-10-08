package ru.ephy.raidhelper.raid.scheduler.raidstatemanager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;

import java.util.HashSet;

public class RaidCacheManager {

    private final JavaPlugin plugin;

    private final double notifyRadius;
    private final int cacheExpirationTime;

    public RaidCacheManager(final JavaPlugin plugin, final Config config) {
        // Initializes required instances
        this.plugin = plugin;

        // Initializes required variables
        notifyRadius = Math.pow(config.getRadius(), 2);
        cacheExpirationTime = config.getCacheExpireTime();
    }

    public void updateCacheIfNeeded(final RaidData raidData) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            raidData.setPlayersWithinRaid(new HashSet<>(raidData.getRaidLocation().getNearbyPlayers(notifyRadius)));
            raidData.setRaidersSet(new HashSet<>(raidData.getRaidInstance().getRaiders()));
        }, cacheExpirationTime);
    }
}

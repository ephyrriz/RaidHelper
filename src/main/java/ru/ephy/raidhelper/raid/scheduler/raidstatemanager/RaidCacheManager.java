package ru.ephy.raidhelper.raid.scheduler.raidstatemanager;

import org.bukkit.entity.Player;
import org.bukkit.entity.Raider;

import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;

import java.util.Set;

public class RaidCacheManager {

    private final double notifyRadius;
    private final int cacheExpirationTime;

    public RaidCacheManager(final Config config) {
        notifyRadius = Math.pow(config.getRadius(), 2);
        cacheExpirationTime = config.getCacheExpireTime();
    }

    public void updateCacheIfNeeded(final RaidData raidData) {
        final long currentTime = System.currentTimeMillis();

        if (currentTime - raidData.getLastUpdateTime() > cacheExpirationTime) {
            raidData.setPlayersWithinRaid((Set<Player>) raidData.getRaidLocation().getNearbyPlayers(notifyRadius));
            raidData.setRaidersSet((Set<Raider>) raidData.getRaidInstance().getRaiders());
            raidData.setLastUpdateTime(currentTime);
        }
    }
}

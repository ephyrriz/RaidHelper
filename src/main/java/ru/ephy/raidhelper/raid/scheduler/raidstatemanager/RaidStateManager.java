package ru.ephy.raidhelper.raid.scheduler.raidstatemanager;

import lombok.RequiredArgsConstructor;

import ru.ephy.raidhelper.raid.data.RaidData;

/**
 * Handles the raid state updates and player
 * notifications during a raid.
 */
@RequiredArgsConstructor
public class RaidStateManager {

    private final RaidCacheManager cacheManager;
    private final RaidWaveProcessor waveProcessor;
    private final NotificationManager notificationManager;

    public void updateRaidState(final RaidData raidData) {
        cacheManager.addRaidData(raidData);

        if (waveProcessor.hasWaveEnded(raidData)) {
            waveProcessor.processWaveEnd(raidData);
        } else {
            waveProcessor.processWaveOngoing(raidData);
            notificationManager.notifyPlayers(raidData);
        }
    }
}

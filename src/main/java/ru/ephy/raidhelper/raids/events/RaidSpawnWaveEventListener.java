package ru.ephy.raidhelper.raids.events;

import lombok.RequiredArgsConstructor;
import org.bukkit.Raid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import ru.ephy.raidhelper.raids.RaidLogic;
import ru.ephy.raidhelper.raids.RaidManager;

/**
 * This class handles the events related to changing waves
 * and resets the counter for the raid if it is an initialized one.
 */
@RequiredArgsConstructor
public class RaidSpawnWaveEventListener implements Listener {
    private final RaidManager raidManager; // RaidManager instance reference

    /**
     * Handles the RaidSpawnWaveEvent when a wave has changed.
     *
     * @param event the RaidSpawnWaveEvent
     */
    @EventHandler
    public void on(final RaidSpawnWaveEvent event) {
        final Raid raid = event.getRaid();
        if (raidManager.isRaidInMap(raid)) {
            final RaidLogic raidLogic = raidManager.getRaidLogic(raid);
            if (raidLogic != null) {
                raidLogic.resetRaidCounter();
            }
        }
    }
}

package ru.ephy.raidhelper.main.events;

import lombok.RequiredArgsConstructor;
import org.bukkit.Raid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidStopEvent;
import ru.ephy.raidhelper.main.RaidManager;

/**
 * This class handles the events related to finishing
 * the raid. When it finishes or stops, it gets removed
 * from the plugin's active raids list.
 */
@RequiredArgsConstructor
public class RaidFinishEventListener implements Listener {
    private final RaidManager raidManager; // RaidManager instance reference

    /**
     * Listens to raid finish event; removes
     * the raid if it was in the active raids map.
     *
     * @param event the RaidFinishEvent
     */
    @EventHandler
    public void on(final RaidFinishEvent event) {
        final Raid raid = event.getRaid();
        if (raidManager.isRaidInMap(raid)) {
            raidManager.removeRaid(raid);
        }
    }

    /**
     * Listens to raid stop event; removes
     * the raid if it was in the active raids map.
     *
     * @param event the RaidStopEvent
     */
    @EventHandler
    public void on(final RaidStopEvent event) {
        final Raid raid = event.getRaid();
        if (raidManager.isRaidInMap(raid)) {
            raidManager.removeRaid(raid);
        }
    }
}

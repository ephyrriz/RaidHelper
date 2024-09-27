package ru.ephy.raidhelper.raids.events;

import lombok.RequiredArgsConstructor;
import org.bukkit.Raid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import ru.ephy.raidhelper.raids.RaidManager;

/**
 * This class handles the events related to finishing
 * the raid. When it finishes, it gets removed from
 * the plugin's active raids list.
 */
@RequiredArgsConstructor
public class RaidEndEvent implements Listener {
    private final RaidManager raidManager;

    /**
     * Listens to raid finish event. Removes
     * the raid if it was in the active raids
     * map.
     *
     * @param event raid finish event.
     */
    @EventHandler
    public void on(final RaidFinishEvent event) {
        final Raid raid = event.getRaid();
        if (raidManager.isRaidInMap(raid)) {
            raidManager.removeRaid(raid);
        }
    }
}

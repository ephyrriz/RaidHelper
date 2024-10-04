package ru.ephy.raidhelper.raid.events.end;

import lombok.RequiredArgsConstructor;
import org.bukkit.Raid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidStopEvent;
import ru.ephy.raidhelper.raid.data.RaidManager;

/**
 * Handles events related to raid completion or termination.
 * When a raid finishes or stops, it is removed from the plugin's active raid list.
 */
@RequiredArgsConstructor
public class RaidEnd implements Listener {
    private final RaidManager raidManager; // Manages active raids

    /**
     * Listens for the RaidFinishEvent and handles the raid's completion.
     *
     * @param event The RaidFinishEvent.
     */
    @EventHandler
    public void on(final RaidFinishEvent event) {
        removeRaid(event.getRaid());
    }

    /**
     * Listens for the RaidStopEvent and handles the raid's termination.
     *
     * @param event The RaidStopEvent.
     */
    @EventHandler
    public void on(final RaidStopEvent event) {
        removeRaid(event.getRaid());
    }


    /**
     * Handles both RaidFinishEvent and RaidStopEvent.
     * Removes the raid from the active raid map if it exists.
     *
     * @param raid The raid instance to be removed.
     */
    private void removeRaid(final Raid raid) {
        raidManager.unregisterRaidIfPresent(raid);
    }
}

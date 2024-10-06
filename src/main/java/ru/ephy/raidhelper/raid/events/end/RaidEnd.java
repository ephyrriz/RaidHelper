package ru.ephy.raidhelper.raid.events.end;

import lombok.RequiredArgsConstructor;
import org.bukkit.Raid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidStopEvent;
import ru.ephy.raidhelper.raid.data.RaidManager;

/**
 * Listens for events related to the completion
 * or termination of raids. This class handles
 * the removal of raids from the active list when
 * they finish or stop.
 */
@RequiredArgsConstructor
public class RaidEnd implements Listener {
    private final RaidManager raidManager; // Manages active raids

    /**
     * Handles the completion of a raid.
     *
     * @param event The event triggered when a raid finishes.
     */
    @EventHandler
    public void on(final RaidFinishEvent event) {
        removeRaid(event.getRaid());
    }

    /**
     * Handles the termination of a raid.
     *
     * @param event The event triggered when a raid stops.
     */
    @EventHandler
    public void on(final RaidStopEvent event) {
        removeRaid(event.getRaid());
    }


    /**
     * Removes the specified raid from the active raids list if present.
     *
     * @param raid The raid instance to be removed.
     */
    private void removeRaid(final Raid raid) {
        raidManager.removeRaidIfPresent(raid);
    }
}

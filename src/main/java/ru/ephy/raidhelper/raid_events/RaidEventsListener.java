package ru.ephy.raidhelper.raidevents;

import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.jetbrains.annotations.NotNull;

public class RaidEventsListener implements Listener {

    private final RaidManager raidManager = RaidManager.getInstance(); // RaidManager's instance.

    @EventHandler
    private void on(@NotNull RaidTriggerEvent event) {
        final Raid startedRaid = event.getRaid();
        final Location startedRaidLocation = startedRaid.getLocation();

        addRaidList(startedRaid, startedRaidLocation);
    }

    @EventHandler
    private void on(@NotNull RaidFinishEvent event) {
        final Raid finishedRaid = event.getRaid();
        final Location finishedRaidLocation = finishedRaid.getLocation();

        raidManager.removeRaid(finishedRaidLocation);
    }

    @EventHandler
    private void on(@NotNull RaidSpawnWaveEvent event) {
        final Raid raid = event.getRaid();
        final Location raidLocation = raid.getLocation();

        final RaidInfo raidInfo = raidManager.getRaidInfo(raidLocation);
        if (raidInfo == null) return;

        raidInfo.resetTimeCounter();
    }

    private void addRaidList(@NotNull final Raid startedRaid, @NotNull final Location startedRaidLocation) {
        raidManager.addRaid(startedRaidLocation, startedRaid);
    }
}

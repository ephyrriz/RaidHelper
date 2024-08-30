package ru.ephy.raidhelper.raid_events;

import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.ephy.raidhelper.raid_events.raid_management.RaidTimeCounter;
import ru.ephy.raidhelper.raid_events.raid_management.RaidManager;

import java.util.logging.Logger;

public class RaidEventsListener implements Listener {

    private final RaidManager raidManager;
    private final Logger logger;

    public RaidEventsListener(@NotNull final JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        this.raidManager = RaidManager.getInstance();
    }

    @EventHandler
    public void on(@NotNull RaidSpawnWaveEvent event) {
        final Raid raid = event.getRaid();
        final Location raidLocation = raid.getLocation();
        if (raidManager.getAllRaidLocations().contains(raidLocation)) { resetRaidTimeCounter(raidLocation); return; }
        addRaidToList(raid, raidLocation);
    }

    @EventHandler
    public void on(@NotNull RaidFinishEvent event) {
        final Raid raid = event.getRaid();
        final Location raidLocation = raid.getLocation();
        removeRaidFromList(raidLocation);
    }

    private void addRaidToList(@NotNull final Raid raid, @NotNull final Location raidLocation) {
        raidManager.addRaid(raid, raidLocation);
        logger.info(() -> "Added raid at location: " + raidLocation);
    }

    private void removeRaidFromList(@NotNull final Location raidLocation) {
        raidManager.removeRaid(raidLocation);
        logger.info(() -> "Removed raid at location: " + raidLocation);
    }

    private void resetRaidTimeCounter(@NotNull Location raidLocation) {
        final RaidTimeCounter raidTimeCounter = raidManager.getRaidTimeCounter(raidLocation);
        if (raidTimeCounter == null) { logger.warning(() -> "RaidTimeCounter is null for location: " + raidLocation); return; }
        raidTimeCounter.resetTimeCounter();
        logger.info(() -> "Reset raid's counter at location: " + raidLocation);
    }
}

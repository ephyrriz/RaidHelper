package ru.ephy.raidhelper.raid_events;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.ephy.raidhelper.files.Config;
import ru.ephy.raidhelper.raid_events.raid_management.RaidTimeCounter;
import ru.ephy.raidhelper.raid_events.raid_management.RaidManager;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class RaidScheduler {

    private final JavaPlugin plugin;
    private final RaidManager raidManager;
    private final Config config;
    private final Logger logger;

    public void startScheduler() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkActiveRaids, 0, config.getFrequency());
        logger.info("RaidScheduler has started successfully.");
    }

    private void checkActiveRaids() {
        final Set<Location> raidLocations = raidManager.getAllRaidLocations();

        for (final Location raidLocation : raidLocations) {
            checkActiveRaid(raidLocation);
        }
    }

    private void checkActiveRaid(final Location raidLocation) {
        final RaidTimeCounter raidTimeCounter = raidManager.getRaidTimeCounter(raidLocation);
        if (raidTimeCounter == null) { logger.warning(() -> "RaidTimeCounter is null at location: " + raidLocation); return; }

        raidTimeCounter.incrementTimeCounter();
        if (raidTimeCounter.getTimeCounter() < config.getTime()) return;

        notifyPlayerNearRaids(raidLocation);
    }

    private void notifyPlayerNearRaids(@NotNull final Location raidLocation) {
        final List<Player> players = raidLocation.getWorld().getPlayers();

        for (final Player player : players) {
            final Location playerLocation = player.getLocation();
            if (playerLocation.distanceSquared(raidLocation) > radiusSqr) continue;

            player.sendActionBar(Component.text(config.getActionBarMessage()));
        }
    }
}

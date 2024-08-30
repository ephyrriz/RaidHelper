package ru.ephy.raidhelper.raid_events;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.ephy.raidhelper.configuration.Config;
import ru.ephy.raidhelper.raid_events.raid_management.RaidTimeCounter;
import ru.ephy.raidhelper.raid_events.raid_management.RaidManager;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class RaidScheduler {

    private final JavaPlugin plugin;
    private final RaidManager raidManager;
    private final Config config;
    private final Logger logger;
    private final int radiusSqr;

    public RaidScheduler(@NotNull final JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.raidManager = RaidManager.getInstance();
        this.config = Config.getInstance();
        this.radiusSqr = config.getRadius() * config.getRadius();

        raidSchedulerTask();
    }

    private void raidSchedulerTask() {
        logger.info("Scheduler has started successfully.");
        Bukkit.getScheduler().runTaskTimer(plugin, this::getActiveRaidsList, 0L, config.getUpdateFrequency());
    }

    private void getActiveRaidsList() {
        final Set<Location> raidLocations = raidManager.getAllRaidLocations();

        for (final Location raidLocation : raidLocations) {
            checkActiveRaid(raidLocation);
        }
    }

    private void checkActiveRaid(@NotNull final Location raidLocation) {
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

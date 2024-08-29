package ru.ephy.raidhelper.raidevents;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ephy.raidhelper.configuration.Config;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class RaidScheduler {

    private final JavaPlugin plugin; // Plugin's instance.
    private final RaidManager raidManager; // RaidManager's instance.
    private final Config config; // Config's instance.
    private final Logger logger; // Logger.

    public RaidScheduler(@NotNull final JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.raidManager = RaidManager.getInstance();
        this.config = Config.getInstance();

        raidSchedulerTask();
    }

    private void raidSchedulerTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::getActiveRaidsList, 0L, config.getUpdateFrequency());
    }

    private void getActiveRaidsList() {
        @NotNull final Set<Location> raidLocations = raidManager.getAllRaidLocations();
        if (raidLocations.isEmpty()) return;

        for (final Location raidLocation : raidLocations) {
            checkActiveRaid(raidLocation);
        }
    }

    private void checkActiveRaid(@NotNull final Location raidLocation) {
        @Nullable final Raid raid = raidManager.getRaid(raidLocation);
        if (raid == null) { logger.warning(() -> "Raid is null at location: " + raidLocation); return; }

        @Nullable final RaidInfo raidInfo = raidManager.getRaidInfo(raidLocation);
        if (raidInfo == null) { logger.warning(() -> "RaidInfo is null at location: " + raidLocation); return; }

        raidInfo.incrementTimeCounter();
        if (raidInfo.getTimeCounter() < config.getTime()) return;

        notifyPlayerNearRaids(raidLocation);
    }

    private void notifyPlayerNearRaids(@NotNull final Location raidLocation) {
        final List<Player> players = raidLocation.getWorld().getPlayers();
        if (players.isEmpty()) return;

        for (final Player player : players) {
            final Location playerLocation = player.getLocation();
            if (playerLocation.distance(raidLocation) > config.getRadius()) continue;

            player.sendActionBar(Component.text(config.getActionBarMessage()));
        }
    }
}

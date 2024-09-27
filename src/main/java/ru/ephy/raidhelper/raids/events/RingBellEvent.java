package ru.ephy.raidhelper.raids.events;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.entity.Raider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.raids.RaidLogic;
import ru.ephy.raidhelper.raids.RaidManager;
import ru.ephy.raidhelper.files.Config;

import java.util.Map;

/**
 * This class handles the events related to ringing a bell
 * and teleports raiders based on the bell's location.
 */
@RequiredArgsConstructor
public class RingBellEvent implements Listener {

    private final JavaPlugin plugin;
    private final RaidManager raidManager;
    private final Config config;

    /**
     * Handles the BellRingEvent when a bell is rung.
     *
     * @param event the BellRingEvent
     */
    @EventHandler
    public void on(final BellRingEvent event) {
        final Location bellLocation = event.getBlock().getLocation();
        if (isWorldValid(bellLocation)) {
            handleRaidTeleport(bellLocation);
        }
    }

    /**
     * Checks if the world of the given location is in the configured world list.
     *
     * @param bellLocation the location of the bell
     * @return true if the world is valid, false otherwise
     */
    private boolean isWorldValid(final Location bellLocation) {
        return config.getWorldList().contains(bellLocation.getWorld());
    }

    /**
     * Handles the teleportation of raiders based on the bell's location.
     *
     * @param bellLocation the location of the bell
     */
    private void handleRaidTeleport(final Location bellLocation) {
        for (final Map.Entry<Raid, RaidLogic> entry : raidManager.getRaidMap().entrySet()) {
            final Raid raid = entry.getKey();
            if (isRaidInRange(bellLocation, raid) && entry.getValue().isBellActive()) {
                teleportRaiders(raid, bellLocation);
            }
        }
    }

    /**
     * Checks if the raid is within the teleportation radius.
     *
     * @param bellLocation the location of the bell
     * @param raid the raid to check
     * @return true if the raid is within range, false otherwise
     */
    private boolean isRaidInRange(final Location bellLocation, final Raid raid) {
        final int radiusSquared = config.getRadius() * config.getRadius();
        return bellLocation.distanceSquared(raid.getLocation()) <= radiusSquared;
    }

    private void teleportRaiders(final Raid raid, final Location location) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            location.add(0, config.getHeight(), 0); // Adjust the Y coordinate for teleportation
            for (final Raider raider : raid.getRaiders()) {
                raider.teleport(location);
            }
        }, config.getDelay());
    }
}

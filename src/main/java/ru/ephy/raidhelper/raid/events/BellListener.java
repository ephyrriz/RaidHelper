package ru.ephy.raidhelper.raid.events;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Raider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.raid.data.RaidData;
import ru.ephy.raidhelper.raid.manager.RaidManager;
import ru.ephy.raidhelper.config.Config;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles the BellRingEvent and teleports raiders
 * to the bell's location if certain conditions are met.
 */
@RequiredArgsConstructor
public class BellListener implements Listener {

    private final JavaPlugin plugin;       // Plugin's instance
    private final RaidManager raidManager; // Manages active raids
    private final Config config;           // Holds plugin configuration settings
    private final Logger logger;           // Logger for debugging

    private double radiusSquared;
    private int height;
    private int delay;

    public void initializeVariables() {
        radiusSquared = config.getRadiusSquared();
        height = config.getHeight();
        delay = config.getDelay();
    }

    /**
     * Handles the BellRingEvent when a bell is rung.
     * Only considers valid worlds and player interactions.
     *
     * @param event the BellRingEvent
     */
    @EventHandler
    public void on(final BellRingEvent event) {
        final Entity entity = event.getEntity();

        if (entity instanceof Player) {
            final Location location = event.getBlock().getLocation();
            final World world = location.getWorld();

            if (isValidWorld(world)) {
                teleportRaidersNearBell(world, location);
            }
        }
    }

    /**
     * Checks if the given world is part of the valid worlds in the config.
     *
     * @param world the world to check
     * @return true if the world is valid, false otherwise
     */
    private boolean isValidWorld(final World world) {
        return config.getWorldList().contains(world);
    }

    /**
     * Teleports raiders near the bell to the bell's location.
     * It looks for active raids and verifies if they are within range.
     *
     * @param world    the world of the bell.
     * @param location the location of the bell.
     */
    private void teleportRaidersNearBell(final World world, final Location location) {
        final Map<Integer, RaidData> raidDataMap = raidManager.getWorldRaidMap().get(world);

        if (raidDataMap != null) {
            raidDataMap.values().stream()
                    .filter(RaidData::isRingable)
                    .filter(raidData -> isWithinRange(raidData.getLocation(), location))
                    .forEach(raidData -> scheduleRaiderTeleport(raidData.getRaid(), location));
        }
    }

    /**
     * Checks if the raid's location is within the teleport range of the bell.
     *
     * @param raidLocation the location of the raid
     * @param bellLocation the location of the bell
     * @return true if the raid is within the teleport range, false otherwise
     */
    private boolean isWithinRange(final Location raidLocation, final Location bellLocation) {
        final double distanceSquared = raidLocation.distanceSquared(bellLocation);
        logger.info("Returned squared distance for isRaidInRange: " + distanceSquared);
        return distanceSquared <= radiusSquared;
    }

    /**
     * Schedules a delayed task to teleport all raiders in the raid to the bell's location.
     *
     * @param raid the raid whose raiders will be teleported
     * @param bellLocation the location to teleport to
     *
     * @throws IllegalArgumentException If an illegal argument was passed during the scheduling process.
     */
    private void scheduleRaiderTeleport(final Raid raid, final Location bellLocation) throws IllegalArgumentException {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            final Location targetLocation = bellLocation.clone().add(0, height, 0); // Adjust the Y coordinate for teleportation
            raid.getRaiders().forEach(raider -> teleportRaider(raider, targetLocation));
        }, delay);
    }

    /**
     * Safely teleports a raider to the target location.
     *
     * @param raider the raider entity to teleport
     * @param targetLocation the location to teleport the raider to
     */
    private void teleportRaider(final Raider raider, final Location targetLocation) {
        raider.teleport(targetLocation);
    }
}

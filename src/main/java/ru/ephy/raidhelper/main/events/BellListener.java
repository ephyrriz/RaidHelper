package ru.ephy.raidhelper.main.events;

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
import ru.ephy.raidhelper.main.RaidData;
import ru.ephy.raidhelper.main.RaidManager;
import ru.ephy.raidhelper.config.Config;

import java.util.Map;
import java.util.logging.Logger;

/**
 * This class handles the events related to ringing a bell
 * and teleports raiders based on the bell's location.
 */
@RequiredArgsConstructor
public class BellListener implements Listener {

    private final JavaPlugin plugin;        // Plugin instance reference
    private final RaidManager raidManager;  // RaidManager instance reference
    private final Config config;            // Holds configuration data
    private final Logger logger;            // Logger instance

    /**
     * Handles the BellRingEvent when a bell is rung.
     *
     * @param event the BellRingEvent
     */
    @EventHandler
    public void on(final BellRingEvent event) {
        final Entity entity = event.getEntity();

        if (entity instanceof Player) {
            final Location bellLocation = event.getBlock().getLocation();

            if (isWorldValid(bellLocation)) {
                handleRaidTeleport(bellLocation);
            }
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
        final World world = bellLocation.getWorld();
        final Map<World, Map<Integer, RaidData>> raidMap = raidManager.getRaidMap();

        if (raidMap.containsKey(world)) {
            final Map<Integer, RaidData> raidDataMap = raidMap.get(world);

            raidDataMap.values().stream()
                    .filter(RaidData::isRingable)
                    .filter(raidData -> isRaidInRange(raidData.getLocation(), bellLocation))
                    .forEach(raidData -> teleportRaiders(raidData.getRaid(), bellLocation));
        }
    }

    /**
     * Checks if the raid is within the teleportation radius.
     *
     * @param bellLocation the location of the bell
     * @param raidLocation the location of the raid
     * @return true if the raid is within range, false otherwise
     */
    private boolean isRaidInRange(final Location raidLocation, final Location bellLocation) {
        logger.info("Returned squared distance for isRaidInRange: " + raidLocation.distanceSquared(bellLocation));
        final double distanceSquared = raidLocation.distanceSquared(bellLocation);
        return distanceSquared <= config.getRadiusSquared();
    }

    /**
     * Teleports raiders to the specified location after a delay.
     *
     * @param raid the raid whose raiders are to be teleported
     * @param bellLocation the location to teleport to
     */
    private void teleportRaiders(final Raid raid, final Location bellLocation) {
        try {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                final Location updatedLocation = bellLocation.clone().add(0, config.getHeight(), 0); // Adjust the Y coordinate for teleportation
                for (final Raider raider : raid.getRaiders()) {
                    raider.teleport(updatedLocation);
                }
            }, config.getDelay());
        } catch (final Exception e) {
            logger.severe("An error occured during teleporting raiders to the bell location: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package ru.ephy.raidhelper.raid.events.bell;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.entity.Raider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.Map;

/**
 * Handles the teleportation of raiders when a
 * bell is rung during a raid. The class manages
 * teleportation range, delay, and cooldown periods.
 */
public class RaidersTeleporter {

    private final JavaPlugin plugin;       // Plugin instance used for scheduling
    private final RaidManager raidManager; // Manages raid data and operations

    private final double radiusSquared;    // Radius squared for calculating distances
    private final int teleportHeight;      // Y offset for teleportation above the bell
    private final int teleportDelayTicks;  // Delay before teleporting raiders
    private final int bellCooldownTicks;   // Cooldown period between bell rings

    @Getter
    @Setter
    private boolean inCooldown = false;    // Tracks if the bell is on cooldown

    /**
     * Constructs the class for a proper work.
     *
     * @param plugin      The plugin instance for scheduling tasks
     * @param raidManager Manages active raids and their data
     * @param config      Holds configuration data like teleport range, delay, and cooldown
     */
    public RaidersTeleporter(final JavaPlugin plugin, final RaidManager raidManager, final Config config) {
        this.plugin = plugin;
        this.raidManager = raidManager;

        radiusSquared = Math.pow(config.getRadius(), 2);
        teleportHeight = config.getHeight();
        teleportDelayTicks = config.getTeleportDelay();
        bellCooldownTicks = config.getBellCooldown();
    }

    /**
     * Initiates the process to teleport raiders when a bell is rung.
     * Triggers a cooldown after teleporting.
     *
     * @param bellWorld    The world where the bell is located
     * @param bellLocation The location of the bell
     */
    public void handleRaidersTeleport(final World bellWorld, final Location bellLocation) {
        inCooldown = true;
        teleportNearbyRaiders(bellWorld, bellLocation);

        // Schedule the cooldown reset after the configured number of ticks
        Bukkit.getScheduler().runTaskLater(plugin, () -> inCooldown = false, bellCooldownTicks);
    }

    /**
     * Teleports raiders within range of the bell to its location.
     * Only "ringable" raids within the configured range will have their raiders teleported.
     *
     * @param bellWorld    The world where the bell is located
     * @param bellLocation The location of the bell
     */
    private void teleportNearbyRaiders(final World bellWorld, final Location bellLocation) {
        final Map<Integer, RaidData> raidDataMap = raidManager.getWorldRaidMap().get(bellWorld);
        final Location targetLocation = bellLocation.clone().add(0, teleportHeight, 0); // Adjust Y-coordinate

        if (raidDataMap != null) {
            raidDataMap.values().stream()
                    .filter(RaidData::isRingable) // Only consider "ringable" raids
                    .filter(raidData -> isWithinBellRange(raidData.getLocation(), bellLocation))
                    .forEach(raidData -> scheduleRaidersTeleport(raidData.getRaid(), targetLocation));
        }
    }

    /**
     * Determines if the raid is within the defined teleport range of the bell.
     *
     * @param raidLocation The location of the raid
     * @param bellLocation The location of the bell
     * @return True if the raid is within range, false otherwise
     */
    private boolean isWithinBellRange(final Location raidLocation, final Location bellLocation) {
        final double distanceSquared = raidLocation.distanceSquared(bellLocation);
        return distanceSquared <= radiusSquared;
    }


    /**
     * Schedules the teleportation of raiders after a delay.
     *
     * @param raid           The raid whose raiders will be teleported
     * @param targetLocation The location to teleport the raiders to
     */
    private void scheduleRaidersTeleport(final Raid raid, final Location targetLocation) {
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                raid.getRaiders().forEach(raider -> teleportRaider(raider, targetLocation)), teleportDelayTicks);
    }

    /**
     * Teleports a single raider to the target location.
     *
     * @param raider         The raider entity to teleport
     * @param targetLocation The location to teleport the raider to
     */
    private void teleportRaider(final Raider raider, final Location targetLocation) {
        raider.teleport(targetLocation);
    }
}

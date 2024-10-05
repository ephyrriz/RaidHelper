package ru.ephy.raidhelper.raid.events.bell;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Raider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;
import ru.ephy.raidhelper.raid.data.RaidManager;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles the teleportation of raiders when a
 * bell is rung during a raid. The class manages
 * teleportation range, delay, and cooldown periods.
 */
public class Teleporter {

    private final JavaPlugin plugin;             // Plugin's instance
    private final RaidManager raidManager;       // Manages the raids map
    private final TeleporterPool teleporterPool; // Teleporter pool
    private final Logger logger;                 // Logger for debugging

    private final Component cooldownMessage;     // Cooldown message
    private final Component someCooldownMessage; // Some raids in cooldown message
    private final double rangeSquared;           // Range; radius; squared
    private final int cooldown;                  // Raid's cooldown before it can teleport raiders again
    private final int teleportDelay;             // Teleport delay; after how many ticks raiders will be teleported
    private final int height;                    // Height above the bell where raiders will be teleported to

    /**
     * Constructs the {@link Teleporter} class.
     *
     * @param plugin      The Plugin's instance for schedulers
     * @param raidManager The Raid Manager used to access raid data
     * @param config      The Config's instance for initializing needed variables
     * @param logger      Logger for logging messages
     */
    public Teleporter(final JavaPlugin plugin, final TeleporterPool teleporterPool,
                      final RaidManager raidManager, final Config config, final Logger logger) {
        this.plugin = plugin;
        this.teleporterPool = teleporterPool;
        this.raidManager = raidManager;
        this.logger = logger;

        cooldownMessage = config.getCooldownMessage();
        someCooldownMessage = config.getSomeInCooldownMessage();
        rangeSquared = Math.pow(config.getRadius(), 2);
        cooldown = config.getBellCooldown();
        teleportDelay = config.getTeleportDelay();
        height = config.getHeight();
    }

    /**
     * Handles the teleport operation.
     *
     * @param player        The player who rang the bell
     * @param bellWorld     Rung bell world
     * @param bellLocation  Rung bell location
     */
    public void handleTeleport(final Player player, final World bellWorld, final Location bellLocation) {
        processRaidData(player, bellWorld, bellLocation);
    }

    /**
     * Processes the raid data of the bell world;
     * iterates its values (raidData) and checks their
     * cooldown status.
     *
     * @param player        The player who rang the bell
     * @param bellWorld     Bell's world to retrieve the needed map of raids
     * @param bellLocation  Bell's location to compare against
     */
    private void processRaidData(final Player player, final World bellWorld, final Location bellLocation) {
        final Map<Integer, RaidData> integerRaidMap = raidManager.getWorldRaidMap().get(bellWorld);
        if (integerRaidMap == null || integerRaidMap.isEmpty()) return;

        boolean allInCooldown = true;   // If all raids are in cooldown
        boolean someInCooldown = false; // If some of raids are not in cooldown

        for (final RaidData raidData : integerRaidMap.values()) {
            if (raidData.isInCooldown()) {
                someInCooldown = true;
            } else {
                allInCooldown = false;
                if (raidData.isRingable()) {
                    checkAndTeleportRaiders(raidData, bellLocation);
                    manageRaidCooldown(raidData);
                }
            }
        }

        notifyPlayerAboutCooldown(player, allInCooldown, someInCooldown);
    }

    /**
     * Notifies the player about cooldown status.
     *
     * @param player         Player to whom the message will be sent
     * @param allInCooldown  All raids are in cooldown
     * @param someInCooldown Some raids are not in cooldown
     */
    private void notifyPlayerAboutCooldown(final Player player, final boolean allInCooldown, final boolean someInCooldown) {
        if (allInCooldown) {
            player.sendMessage(cooldownMessage);
        } else if (someInCooldown) {
            player.sendMessage(someCooldownMessage);
        }
    }

    /**
     * Manages cooldown of the RaidData.
     *
     * @param raidData RaidData to control cooldown
     */
    private void manageRaidCooldown (final RaidData raidData) {
        raidData.setInCooldown(true);
        Bukkit.getScheduler().runTaskLater(plugin, () -> raidData.setInCooldown(false), cooldown);
    }


    /**
     * Checks if the raid is within range of the bell's location
     * and teleports raiders if it is.
     *
     * @param raidData      RaidData to retrieve location
     * @param bellLocation  Bell's location to compare against
     */
    private void checkAndTeleportRaiders(final RaidData raidData, final Location bellLocation) {
        if (isWithinTheRaidRange(raidData.getLocation(), bellLocation)) {
            final Location targetLocation = bellLocation.clone().add(0, height, 0);
            scheduleTeleportRaiders(raidData.getRaid(), targetLocation);
        }
    }

    /**
     * Verifies if the distance between bell's and raid's locations
     * is within the squared range.
     *
     * @param raidLocation Raid's location
     * @param bellLocation Bell's location
     * @return true if the distance is within the range, false otherwise
     */
    private boolean isWithinTheRaidRange(final Location raidLocation, final Location bellLocation) {
        return raidLocation.distanceSquared(bellLocation) < rangeSquared;
    }

    /**
     * Schedules raiders teleport after a delay.
     *
     * @param raid           Raid to get raiders from
     * @param targetLocation Target location for teleporting raiders
     */
    private void scheduleTeleportRaiders(final Raid raid, final Location targetLocation) {
        if (raid == null || targetLocation == null) {
            logger.warning("Raid or target location is null. Cannot schedule teleport.");
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            final List<Raider> raiderList = raid.getRaiders();

            if (!raiderList.isEmpty()) {
                for (final Raider raider : raiderList) {
                    teleportRaider(raider, targetLocation);
                }
            }
        }, teleportDelay);

        teleporterPool.releaseHandler(this);
    }

    /**
     * Teleports a raider to the target location.
     *
     * @param raider         Raider entity
     * @param targetLocation Target location for teleporting the raider
     */
    private void teleportRaider(final Raider raider, final Location targetLocation) {
        raider.teleport(targetLocation);
    }
}

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

import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages teleportation of raiders when a bell is rung during a raid.
 * Handles range, delay, cooldowns, and triggers teleportation.
 */
public class Teleporter {

    private final JavaPlugin plugin;         // Plugin instance for scheduling
    private final RaidManager raidManager;   // Manages active raids
    private final TeleporterPool pool;       // Pool of reusable teleporters
    private final Logger logger;             // Logger for messages

    private final Component cooldownMsg;     // Message shown when raid is on cooldown
    private final Component partialCooldownMsg; // Message for partial cooldowns
    private final double rangeSquared;       // Effective teleport range (squared)
    private final int cooldownTime;          // Cooldown duration (ticks)
    private final int delay;                 // Delay before teleport (ticks)
    private final int heightOffset;          // Height offset for teleport location

    /**
     * Initializes the Teleporter instance.
     *
     * @param plugin      The main plugin instance for tasks
     * @param pool        Teleporter pool to manage instances
     * @param raidManager Raid data manager
     * @param config      Configuration settings
     * @param logger      Logger for debugging and info
     */
    public Teleporter(final JavaPlugin plugin, final TeleporterPool pool,
                      final RaidManager raidManager, final Config config, final Logger logger) {
        this.plugin = plugin;
        this.pool = pool;
        this.raidManager = raidManager;
        this.logger = logger;

        cooldownMsg = config.getCooldownWarning();
        partialCooldownMsg = config.getPartialCooldownWarning();
        rangeSquared = Math.pow(config.getRadius(), 2); // Calculate range squared
        cooldownTime = config.getBellCooldown();
        delay = config.getTeleportDelay();
        heightOffset = config.getHeight();
    }


    /**
     * Initiates raider teleportation when a bell is rung.
     *
     * @param player           The player who triggered the bell
     * @param bellWorld        World where the bell was rung
     * @param bellLocation     Bell's location
     */
    public void handleTeleport(final Player player, final World bellWorld, final Location bellLocation) {
        processRaid(player, bellWorld, bellLocation);
    }

    /**
     * Processes all raids in the specified world,
     * checking cooldowns and triggering teleportation.
     *
     * @param player           The player who rang the bell
     * @param bellWorld        The world to search for raids
     * @param bellLocation     The bell's location
     */
    private void processRaid(final Player player, final World bellWorld, final Location bellLocation) {
        final Map<Integer, RaidData> integerRaidMap = raidManager.getActiveRaidsByWorld().get(bellWorld);
        if (integerRaidMap == null || integerRaidMap.isEmpty()) {
            logger.warning("The integerRaidMap is null or empty. Cannot process it.");
            return;
        }

        boolean allOnCooldown = true;   // If all raids are in cooldown
        boolean someOnCooldown = false; // If some of raids are not in cooldown

        for (final RaidData raidData : integerRaidMap.values()) {
            if (raidData.isCooldownActive()) {
                someOnCooldown = true;
            } else {
                allOnCooldown = false;
                if (raidData.isTeleportEnabled() && isWithinRange(raidData.getRaidLocation(), bellLocation)) {
                    teleportEligibleRaiders(raidData, bellLocation);
                    setRaidCooldown(raidData);
                    pool.returnTeleporter(this);
                }
            }
        }

        sendCooldownMessage(player, allOnCooldown, someOnCooldown);
    }

    /**
     * Teleports raiders from eligible raids near the bell location.
     *
     * @param raidData      Raid instance containing raiders
     * @param bellLocation  Bell's location to compare distance
     */
    private void teleportEligibleRaiders(final RaidData raidData, final Location bellLocation) {
        final Location targetLocation = bellLocation.clone().add(0, heightOffset, 0);
        scheduleRaiderTeleport(raidData.getRaidInstance(), targetLocation);
    }

    /**
     * Checks if the raid's location is within the bell's teleport range.
     *
     * @param raidLocation  The location of the raid
     * @param bellLocation  The location of the bell
     * @return true if within range, false otherwise
     */
    private boolean isWithinRange(final Location raidLocation, final Location bellLocation) {
        return raidLocation.distanceSquared(bellLocation) < rangeSquared;
    }

    /**
     * Schedules the teleportation of raiders after a specified delay.
     *
     * @param raid           The raid entity to teleport raiders from
     * @param targetLocation The target teleportation location
     */
    private void scheduleRaiderTeleport(final Raid raid, final Location targetLocation) {
        if (raid == null || targetLocation == null) {
            logger.warning("Raid or target location is null. Cannot schedule teleport.");
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () ->
                raid.getRaiders().forEach(raider -> teleport(raider, targetLocation)), delay);
    }

    /**
     * Teleports an individual raider to the target location.
     *
     * @param raider         The raider entity to teleport
     * @param targetLocation The destination location
     */
    private void teleport(final Raider raider, final Location targetLocation) {
        raider.teleport(targetLocation);
    }

    /**
     * Puts the raid into cooldown mode after teleporting raiders.
     *
     * @param raidData Raid to put on cooldown
     */
    private void setRaidCooldown(final RaidData raidData) {
        raidData.setCooldownActive(true);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                raidData.setCooldownActive(false), cooldownTime);
    }

    /**
     * Sends cooldown messages to the player based on the raid's cooldown status.
     *
     * @param player        The player to notify
     * @param allOnCooldown Whether all raids are in cooldown
     * @param someOnCooldown Whether some raids are in cooldown
     */
    private void sendCooldownMessage(final Player player, final boolean allOnCooldown, final boolean someOnCooldown) {
        if (allOnCooldown) {
            player.sendMessage(cooldownMsg);
        } else if (someOnCooldown) {
            player.sendMessage(partialCooldownMsg);
        }
    }
}

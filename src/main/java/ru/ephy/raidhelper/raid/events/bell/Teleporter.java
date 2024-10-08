package ru.ephy.raidhelper.raid.events.bell;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
 * Handles raider teleportation when a bell rings during a raid.
 * Manages range, delay, cooldown, and triggers teleportation.
 */
public class Teleporter {

    private final JavaPlugin plugin;                  // Plugin instance for scheduling
    private final RaidManager raidManager;            // Manages active raids
    private final TeleporterPool pool;                // Reusable teleporter pool
    private final Logger logger;                      // Logger for debug and info

    private final Component teleportMessage;          // Message when teleport is successful
    private final Component cooldownMessage;          // Message when raid is on cooldown
    private final Component partialCooldownMesssage;  // Message for partial cooldowns
    private final double teleportRadiusSquared;       // Teleport range (squared)
    private final int cooldownDuration;               // Cooldown duration (ticks)
    private final int delay;                          // Delay before teleport (ticks)
    private final int teleportHeightOffset;           // Height offset for teleport location

    /**
     * Initializes Teleporter with configuration and resources.
     *
     * @param plugin      Main plugin instance
     * @param pool        Teleporter pool
     * @param raidManager Manages raids
     * @param config      Configuration
     * @param logger      For logging information
     */
    public Teleporter(final JavaPlugin plugin, final TeleporterPool pool,
                      final RaidManager raidManager, final Config config, final Logger logger) {
        // Initializes required instances
        this.plugin = plugin;
        this.pool = pool;
        this.raidManager = raidManager;
        this.logger = logger;

        // Initializes required variables
        cooldownMessage = config.getCooldownWarning();
        partialCooldownMesssage = config.getPartialCooldownWarning();
        teleportMessage = config.getTeleportMessage();

        teleportRadiusSquared = Math.pow(config.getRadius(), 2); // Calculate radius squared
        cooldownDuration = config.getBellCooldown();
        delay = config.getTeleportDelay();
        teleportHeightOffset = config.getHeight();
    }

    /**
     * Starts raider teleportation when a bell is rung.
     *
     * @param player       The player ringing the bell
     * @param bellWorld    The world of the bell
     * @param bellLocation Location of the bell
     */
    public void initiateTeleport(final Player player, final World bellWorld, final Location bellLocation) {
        processRaidsInWorld(player, bellWorld, bellLocation);
    }

    /**
     * Processes raids in the world, checks cooldowns, and teleports raiders.
     *
     * @param player       The player who rang the bell
     * @param bellWorld    World where the bell is
     * @param bellLocation Bell's location
     */
    private void processRaidsInWorld(final Player player, final World bellWorld, final Location bellLocation) {
        final Map<Integer, RaidData> raidMap = raidManager.getActiveRaidsByWorld().get(bellWorld);
        if (raidMap == null || raidMap.isEmpty()) {
            logger.warning("No raids to process in world: " + bellWorld.getName());
            return;
        }

        boolean allOnCooldown = true;   // If all raids are in cooldown
        boolean someOnCooldown = false; // If some of raids are not in cooldown

        for (final RaidData raidData : raidMap.values()) {
            if (raidData.isTeleportEnabled() && isWithinTeleportRange(raidData.getRaidLocation(), bellLocation)) {
                if (raidData.isCooldownActive()) {
                    someOnCooldown = true;
                } else {
                    allOnCooldown = false;
                    teleportRaiders(raidData, bellLocation);
                    activateCooldown(raidData);
                }
            }
        }
        sendMessage(player, allOnCooldown, someOnCooldown);
        pool.returnTeleporter(this);
    }

    /**
     * Teleports raiders from the raid near the bell.
     *
     * @param raidData     The raid to teleport raiders from
     * @param bellLocation Location of the bell
     */
    private void teleportRaiders(final RaidData raidData, final Location bellLocation) {
        final Location targetLocation = bellLocation.clone().add(0, teleportHeightOffset, 0);
        scheduleTeleportUpdate(raidData, targetLocation);
    }

    /**
     * Checks if the raid's location is within the bell's teleport range.
     *
     * @param raidLocation Location of the raid
     * @param bellLocation Location of the bell
     * @return true if within range, false otherwise
     */
    private boolean isWithinTeleportRange(final Location raidLocation, final Location bellLocation) {
        return raidLocation.distanceSquared(bellLocation) < teleportRadiusSquared;
    }

    /**
     * Schedules raider teleportation with a delay.
     *
     * @param targetLocation Target location for teleportation
     */
    private void scheduleTeleportUpdate(final RaidData raidData, final Location targetLocation) {
        if (targetLocation == null) {
            logger.warning("Raid or target location is null. Cannot schedule teleport.");
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () ->
                raidData.getRaidersSet().forEach(raider -> teleportRaider(raider, targetLocation)), delay);
    }

    /**
     * Teleports a single raider to the target location.
     *
     * @param raider         Raider entity to teleport
     * @param targetLocation Target teleport location
     */
    private void teleportRaider(final Raider raider, final Location targetLocation) {
        raider.teleport(targetLocation);
    }

    /**
     * Puts the raid into cooldown after teleporting raiders.
     *
     * @param raidData The raid to set cooldown for
     */
    private void activateCooldown(final RaidData raidData) {
        raidData.setCooldownActive(true);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                raidData.setCooldownActive(false), cooldownDuration);
    }

    /**
     * Sends cooldown messages based on raid status.
     *
     * @param player        Player who rang the bell
     * @param allOnCooldown If all raids are on cooldown
     * @param someOnCooldown If some raids are on cooldown
     */
    private void sendMessage(final Player player, final boolean allOnCooldown, final boolean someOnCooldown) {
        if (allOnCooldown) {
            player.sendMessage(cooldownMessage);
        } else if (someOnCooldown) {
            player.sendMessage(partialCooldownMesssage);
        } else {
            player.sendMessage(teleportMessage);
        }
    }
}

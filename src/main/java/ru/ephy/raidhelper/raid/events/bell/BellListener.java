package ru.ephy.raidhelper.raid.events.bell;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.raid.data.RaidManager;
import ru.ephy.raidhelper.config.Config;

import java.util.List;

/**
 * The BellListener class listens for bell ring
 * events and handles the teleportation of
 * raiders to the bell's location if the bell is
 * rung in a valid world.
 */
public class BellListener implements Listener {

    private final RaidersTeleporter raidersTeleporter; // Handles the teleportation logic for raiders
    private final List<World> worldList;               // List of worlds where raider teleportation is enabled
    private final Component cooldownMessage;                   // Message that will be sent to a player if the bell is in cooldown

    /**
     * Constructs the class for a proper work.
     *
     * @param plugin      The plugin instance used for scheduling tasks
     * @param raidManager The RaidManager instance to track raids
     * @param config      The configuration for allowed worlds and teleport settings
     */
    public BellListener(final JavaPlugin plugin, final RaidManager raidManager, final Config config) {
        raidersTeleporter = new RaidersTeleporter(plugin, raidManager, config);
        worldList = config.getWorldList();
        cooldownMessage = config.getCooldownMessage();
    }

    /**
     * Handles the {@link BellRingEvent} when a player rings a bell.
     * If the bell is in an allowed world, the raiders are teleported
     * to the bell's location.
     *
     * @param event BellRingEvent
     */
    @EventHandler
    public void on(final BellRingEvent event) {
        final Entity entity = event.getEntity();

        if (entity instanceof final Player player) {
            final Location bellLocation = event.getBlock().getLocation();
            final World bellWorld = bellLocation.getWorld();

            if (isWorldInList(bellWorld)) {
                if (!raidersTeleporter.isInCooldown()) {
                    raidersTeleporter.handleRaidersTeleport(bellWorld, bellLocation);
                } else {
                    player.sendMessage(cooldownMessage);
                }
            }
        }
    }

    /**
     * Checks whether the provided world is part of the
     * allowed worlds for raider teleportation.
     *
     * @param world The world to check
     * @return True if the world is allowed, false otherwise
     */
    private boolean isWorldInList(final World world) {
        return worldList.contains(world);
    }
}

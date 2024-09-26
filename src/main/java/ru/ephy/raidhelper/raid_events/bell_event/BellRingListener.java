package ru.ephy.raidhelper.raid_events.bell_event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.ephy.raidhelper.files.Config;
import ru.ephy.raidhelper.raid_events.raid_management.RaidTimeCounter;
import ru.ephy.raidhelper.raid_events.raid_management.RaidManager;

import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;

public class BellRingListener implements Listener {

    private final JavaPlugin plugin;
    private final RaidManager raidManager;
    private final Config config;
    private final Logger logger;
    private final Set<EntityType> raiderTypes;
    private final int radiusSqr;
    private final int radius;
    private final int teleportDelay;

    public BellRingListener(@NotNull final JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.raidManager = RaidManager.getInstance();
        this.config = Config.getInstance();
        this.raiderTypes = EnumSet.of(EntityType.RAVAGER, EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER, EntityType.WITCH);
        this.radiusSqr = config.getRadius() * config.getRadius();
        this.radius = config.getRadius();
        this.teleportDelay = config.getTeleportDelay();
    }

    @EventHandler
    public void on(@NotNull BellRingEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;

        final Block block = event.getBlock();
        final Location bellLocation = block.getLocation();

        for (final Location raidLocation : raidManager.getAllRaidLocations()) {
            final RaidTimeCounter raidTimeCounter = raidManager.getRaidTimeCounter(raidLocation);
            final Location bellLocationUpdated = bellLocation.clone().add(0, config.getHeightOffset(), 0);

            if (bellLocation.distanceSquared(raidLocation) <= radiusSqr &&
                raidTimeCounter != null && raidTimeCounter.isRaidDurationExceeded()) {

                runTaskTeleportRaidersToBellLater(raidLocation, bellLocationUpdated);
            }
        }
    }

    private void runTaskTeleportRaidersToBellLater(@NotNull final Location raidLocation, @NotNull final Location bellLocation) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> teleportRaidersToBell(raidLocation, bellLocation), teleportDelay);
    }

    private void teleportRaidersToBell(@NotNull final Location raidLocation, @NotNull final Location bellLocation) {
        logger.info(() -> "Teleporting raiders to bell at location: " + bellLocation);
        for (final Entity resonatedEntity : raidLocation.getNearbyLivingEntities(radius)) {
            if (resonatedEntity instanceof LivingEntity) {
                final EntityType resonatedEntityType = resonatedEntity.getType();

                if (raiderTypes.contains(resonatedEntityType)) {
                    resonatedEntity.teleport(bellLocation);
                }
            }
        }
    }
}

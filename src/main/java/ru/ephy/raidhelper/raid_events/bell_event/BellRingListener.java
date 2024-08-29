package ru.ephy.raidhelper.raid_events.bell_event;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.ephy.raidhelper.configuration.Config;
import ru.ephy.raidhelper.raid_events.raid_management.RaidTimeCounter;
import ru.ephy.raidhelper.raid_events.raid_management.RaidManager;

import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;

public class BellResonationListener implements Listener {

    private final RaidManager raidManager;
    private final Config config;
    private final Logger logger;
    private final Set<EntityType> raiderTypes;
    private final int radiusSqr;
    private final int radius;

    public BellResonationListener(@NotNull final JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        this.raidManager = RaidManager.getInstance();
        this.config = Config.getInstance();
        this.raiderTypes = EnumSet.of(EntityType.RAVAGER, EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER, EntityType.WITCH);
        this.radiusSqr = config.getRadius() * config.getRadius();
        this.radius = config.getRadius();
    }

    @EventHandler
    public void on(@NotNull BellRingEvent event) {
        final Block block = event.getBlock();
        if (block.getType() != Material.BELL) return;

        final Location bellLocation = block.getLocation();

        for (final Location raidLocation : raidManager.getAllRaidLocations()) {
            final RaidTimeCounter raidTimeCounter = raidManager.getRaidTimeCounter(raidLocation);
            final Location bellLocationUpdated = bellLocation.clone().add(0, config.getHeightOffset(), 0);

            if (bellLocation.distanceSquared(raidLocation) <= radiusSqr &&
                raidTimeCounter != null && raidTimeCounter.isRaidDurationExceeded()) {

                logger.info(() -> "Teleporting raiders to bell at location: " + bellLocationUpdated);
                teleportRaidersToBell(raidLocation, bellLocationUpdated);
            }
        }
    }

    private void teleportRaidersToBell(@NotNull final Location raidLocation, @NotNull final Location bellLocation) {
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

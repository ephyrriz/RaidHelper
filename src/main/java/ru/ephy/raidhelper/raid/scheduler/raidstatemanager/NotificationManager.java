package ru.ephy.raidhelper.raid.scheduler.raidstatemanager;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;

import java.util.HashSet;
import java.util.Set;

public class NotificationManager {
    private final Component actionBarMessage;
    private final double notifyRadius;

    public NotificationManager(final Config config) {
        actionBarMessage = config.getRingMessage();
        notifyRadius = Math.pow(config.getRadius(), 2);
    }

    public void notifyPlayers(final RaidData raidData) {
        if (raidData.isTeleportEnabled()) {
            final Set<Player> players = raidData.getPlayersWithinRaid() != null
                    ? raidData.getPlayersWithinRaid()
                    : new HashSet<>(raidData.getRaidLocation().getNearbyPlayers(notifyRadius));

            players.forEach(player -> player.sendActionBar(actionBarMessage));
        }
    }
}

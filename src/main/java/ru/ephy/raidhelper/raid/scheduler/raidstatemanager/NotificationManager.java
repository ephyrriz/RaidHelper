package ru.ephy.raidhelper.raid.scheduler.raidstatemanager;

import net.kyori.adventure.text.Component;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;

public class NotificationManager {
    private final Component actionBarMessage;

    public NotificationManager(final Config config) {
        actionBarMessage = config.getRingMessage();
    }

    public void notifyPlayers(final RaidData raidData) {
        if (raidData.isTeleportEnabled()) {
            raidData.getPlayersWithinRaid().forEach(player ->
                    player.sendActionBar(actionBarMessage));
        }
    }
}

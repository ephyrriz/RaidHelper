package ru.ephy.raidhelper.raids.events;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import ru.ephy.raidhelper.raids.RaidLogic;
import ru.ephy.raidhelper.raids.RaidManager;

@RequiredArgsConstructor
public class RaidNextWaveEvent implements Listener {
    private final RaidManager raidManager;

    @EventHandler
    public void on(final RaidSpawnWaveEvent event) {
        if (raidManager.isRaidInMap(event.getRaid())) {
            RaidLogic raidLogic = raidManager.getRaidLogic(event.getRaid());
            if (raidLogic != null) {
                raidLogic.resetCounter();
            }
        }
    }
}

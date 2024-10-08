package ru.ephy.raidhelper.raid.scheduler.raidstatemanager;

import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.data.RaidData;

public class RaidWaveProcessor {
    private final int bellWorkDelay;

    public RaidWaveProcessor(final Config config) {
        bellWorkDelay = config.getBellWorkDelay();
    }

    public boolean hasWaveEnded(final RaidData raidData) {
        return raidData.getRaidersSet().isEmpty();
    }

    public void processWaveEnd(final RaidData raidData) {
        if (raidData.isCounterResetAllowed()) {
            raidData.setCounterResetAllowed(false);
            raidData.setTeleportEnabled(false);
            raidData.resetCounter();
        }
    }

    public void processWaveOngoing(final RaidData raidData) {
        if (!raidData.isTeleportEnabled()) {
            if (raidData.getTickCounter() > bellWorkDelay) {
                raidData.setTeleportEnabled(true);
                raidData.setCounterResetAllowed(true);
            } else {
                raidData.incrementCounter();
            }
        }
    }
}

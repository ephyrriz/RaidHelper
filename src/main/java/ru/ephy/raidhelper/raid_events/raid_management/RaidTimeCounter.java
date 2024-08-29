package ru.ephy.raidhelper.raid_events.raid_management;

import ru.ephy.raidhelper.configuration.Config;

public class RaidInfo {

    private final Config config = Config.getInstance(); // Instance of the config file.
    private final int time = config.getTime(); // Time after which the next phase starts.
    private int raidTimeCounter; // Counter.

    public RaidInfo() {
        this.raidTimeCounter = 0;
    }

    public int getTimeCounter() {
        return raidTimeCounter;
    }

    public void incrementTimeCounter() {
        this.raidTimeCounter++;
    }

    public void resetTimeCounter() {
        this.raidTimeCounter = 0;
    }

    public boolean isRaidDurationExceeded() {
        return raidTimeCounter > time;
    }
}

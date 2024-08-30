package ru.ephy.raidhelper.raid_events.raid_management;

import ru.ephy.raidhelper.configuration.Config;

public class RaidTimeCounter {

    private final Config config = Config.getInstance();

    private final int time = config.getTime();
    private int counter;

    public RaidTimeCounter() {
        this.counter = 0;
    }

    public int getTimeCounter() {
        return counter;
    }

    public void incrementTimeCounter() {
        this.counter++;
    }

    public void resetTimeCounter() {
        this.counter = 0;
    }

    public boolean isRaidDurationExceeded() {
        return counter > time;
    }
}

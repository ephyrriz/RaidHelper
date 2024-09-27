package ru.ephy.raidhelper.raids;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.files.Config;

@Getter
@RequiredArgsConstructor
public class RaidLogic {

    private final JavaPlugin plugin;
    private final Config config;
    private final Raid raid;

    private Location location;
    private boolean isBellActive;
    private long activeTicks;

    public void startCounter() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::incrementCounter, 0, config.getFrequencyRaid());
    }

    private void incrementCounter() {

    }
}

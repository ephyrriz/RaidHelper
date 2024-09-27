package ru.ephy.raidhelper.raids;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.files.Config;

@Getter
@RequiredArgsConstructor
public class RaidLogic {

    private final JavaPlugin plugin;
    private final Config config;
    private final Raid raid;

    private boolean isBellActive = false;
    private long activeTicks = 0;

    private int taskId = -1;

    public void startCounter() {
        taskId = Bukkit.getScheduler().runTaskTimer(
                plugin, this::checkTime, 0, config.getFrequencyRaid()).getTaskId();
    }

    private void checkTime() {
        if (!isBellActive) {
            activeTicks += 1;
            if (activeTicks > config.getCooldown()) {
                isBellActive = true;
            }
        }
    }

    public void resetCounter() {
        activeTicks = 0;
        isBellActive = false;
    }

    public void stopCounter() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}

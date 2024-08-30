package ru.ephy.raidhelper.configuration;

import lombok.NoArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class Config {

    private static Config instance;
    private static String actionBarMessage;
    private static int heightOffset;
    private static int radius;
    private static int time;
    private static int updateFrequency;
    private static int teleportDelay;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public static void load(@NotNull FileConfiguration file) {
        heightOffset = file.getInt("height_above_bell", 10);
        radius = file.getInt("radius", 50);
        actionBarMessage = file.getString("action_bar_message", "If you can't find raiders just ring a bell, and they will spawn above it.");
        time = file.getInt("bell_cooldown_seconds", 60);
        updateFrequency = file.getInt("update_frequency", 20);
        teleportDelay = file.getInt("teleport_delay", 60);
    }

    public String getActionBarMessage() { return actionBarMessage; }

    public int getTime() {
        return time;
    }

    public int getRadius() {
        return radius;
    }

    public int getHeightOffset() { return heightOffset; }

    public int getUpdateFrequency() { return updateFrequency; }

    public int getTeleportDelay() {
        return teleportDelay;
    }
}

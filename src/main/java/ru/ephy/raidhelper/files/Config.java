package ru.ephy.raidhelper.files;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * This class retrieves the values from the config to use
 * them in the rest part of the plugin.
 */
@Getter
@RequiredArgsConstructor
public class Config {

    private final FileConfiguration fileConfig; // File config

    private String message;                     // Action bar message that is shown for players.
    private int height;                         // How far above raiders will be teleported.
    private int radius;                         // The radious of the bell's effect.
    private int cooldown;                       // After how many ticks will work.
    private int frequency;                      // How often check for the raids.
    private int delay;                          // After how many ticks teleport the raiders.

    /**
     * initailizes the file config and loads necessary values.
     */
    public void initialize() {
        loadValues();
    }

    /**
     * The method loads the values.
     */
    private void loadValues() {
        message = fileConfig.getString("settings.message", "If you can't find the raiders, just ring a bell and they will spawn above it.");
        height = fileConfig.getInt("settings.height", 10);
        radius = fileConfig.getInt("settings.radius", 50);
        cooldown = fileConfig.getInt("settings.cooldown", 1200);
        frequency = fileConfig.getInt("settings.frequency", 20);
        delay = fileConfig.getInt("settings.delay", 60);
    }
}

package ru.ephy.raidhelper.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * This class retrieves the values from the config to use
 * them in the rest part of the plugin.
 */
@Getter
@RequiredArgsConstructor
public class Config {

    private static final String MAIN_SETTINGS = "settings.main";         // Main settings
    private static final String ADVANCED_SETTINGS = "settings.advanced"; // Advanced settings
    private static final String WORLD_SETTINGS = "settings.worlds";      // World settings

    private final JavaPlugin plugin;            // Plugin's instance
    private final FileConfiguration fileConfig; // Plugin's configuration file
    private final Logger logger;                // Logger for reporting errors and information

    private List<World> worldList;              // List of valid worlds from the configuration
    private String message;                     // Message shown to players when action bar is triggered
    private double radiusSquared;               // The squared radius for bell teleportation effect
    private int height;                         // The height above which raiders will be teleported
    private int cooldown;                       // The cooldown time before another teleportation can occur
    private int frequencyWorld;                 // Frequency for checking world raids
    private int frequencyRaid;                  // Frequency for checking raids statuses
    private int delay;                          // Delay before raiders are teleported

    /**
     * Loads configuration values from the config file and validates them.
     * If any critical values are missing or incorrect, the plugin will be disabled.
     */
    public void loadValues() {
        try {
            loadMainSettings();
            loadAdvancedSettings();
            loadWorldSettings();

            if (worldList.isEmpty()) {
                logger.severe("No valid worlds found in config. Disabling the plugin.");
                disablePlugin();
            }
        } catch (final Exception e) {
            logger.severe("Error loading config values: " + e.getMessage());
            e.printStackTrace();
            disablePlugin();
        }
    }

    /**
     * Loads main settings.
     */
    private void loadMainSettings() {
        message = fileConfig.getString(MAIN_SETTINGS + ".message",
                "If you can't find the raiders, just ring a bell and they will spawn above it.");
        height = verifyNonNegInt(MAIN_SETTINGS + ".height", 10);
        radiusSquared = Math.pow(verifyNonNegDouble(MAIN_SETTINGS + ".radius", 50), 2);
        cooldown = verifyNonNegInt(MAIN_SETTINGS + ".cooldown", 60);
        delay = verifyNonNegInt(MAIN_SETTINGS + ".delay", 60);
    }

    /**
     * Loads advanced settings.
     */
    private void loadAdvancedSettings() {
        frequencyWorld = verifyNonNegInt(ADVANCED_SETTINGS + ".frequencyWorld", 100);
        frequencyRaid = verifyNonNegInt(ADVANCED_SETTINGS + ".frequencyRaid", 20);
    }

    /**
     * Loads world settings.
     */
    private void loadWorldSettings() {
        worldList = loadWorldList();
    }

    /**
     * Loads and validates the list of worlds from the configuration.
     *
     * @return a list of valid worlds
     */
    @NotNull
    private List<World> loadWorldList() {
        final List<String> worldNames = fileConfig.getStringList(WORLD_SETTINGS);
        return new ArrayList<>(
                worldNames.stream()
                        .map(Bukkit::getWorld)
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    /**
     * Validates that the provided configuration value is a non-negative integer.
     * If the value is invalid, the default value is used.
     *
     * @param path the configuration path
     * @param defaultValue the default value to use if the config value is invalid
     * @return a valid non-negative integer from the config or the default value
     */
    private int verifyNonNegInt(final String path, final int defaultValue) {
        final int value = fileConfig.getInt(path, defaultValue);
        if (value < 0) {
            logger.warning("Config value at '" + path + "' cannot be negative. Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    /**
     * Validates that the provided configuration value is a non-negative double.
     * If the value is invalid, the default value is used.
     *
     * @param path the configuration path
     * @param defaultValue the default value to use if the config value is invalid
     * @return a valid non-negative double from the config or the default value
     */
    private double verifyNonNegDouble(final String path, final double defaultValue) {
        final double value = fileConfig.getDouble(path, defaultValue);
        if (value < 0) {
            logger.warning("Config value at '" + path + "' cannot be negative. Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    /**
     * Disables the plugin in case of critical configuration errors.
     */
    private void disablePlugin() {
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }
}

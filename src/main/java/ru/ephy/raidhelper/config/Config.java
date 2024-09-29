package ru.ephy.raidhelper.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Config class handles loading and validating plugin
 * configuration values.
 */
@Getter
@RequiredArgsConstructor
public class Config {

    private static final String SETTINGS = "settings";              // Main settings
    private static final String RAID_CHECK = "settings.raid_check"; // Advanced settings
    private static final String WORLDS = "settings.worlds";         // World settings

    private final JavaPlugin plugin;            // Plugin's instance
    private final FileConfiguration fileConfig; // Plugin's configuration file
    private final Logger logger;                // Logger for debugging

    private RaidCheckMode raidCheckMode;        // The way active raids in the worlds are being checked
    private List<World> worldList;              // List of valid worlds from the configuration
    private Component message;                  // Message shown to players when action bar is triggered
    private double radius;                      // The squared radius for bell teleportation effect
    private int height;                         // The height above which raiders will be teleported
    private int bellCooldown;                   // The cooldown time before another teleportation can occur
    private int bellWorkAfter;                  // The time since the start of a wave before the bell will work
    private int worldCheckFrequency;            // Frequency for checking world raids
    private int maxChecksPerTick;               // Max checks per tick for raids within worlds
    private int raidCheckFrequency;             // Frequency for checking raids statuses
    private int teleportDelay;                  // Delay before raiders are teleported

    /**
     * Enum representing the raid check modes.
     */
    public enum RaidCheckMode {
        SCHEDULER,
        EVENT
    }

    /**
     * Loads and validates configuration values.
     * Disables the plugin if the world list is empty.
     */
    public void loadValues() {
        loadMainSettings();
        loadRaidCheckSettings();
        loadWorldList();

        if (worldList.isEmpty()) {
            logger.severe("No valid worlds found in config. Disabling the plugin.");
            disablePlugin();
        }
    }

    // 1. Load Methods

    /**
     * Loads the main settings for the plugin.
     */
    private void loadMainSettings() {
        message = getComponent(SETTINGS + ".message", "If you can't find the raiders, just ring a bell and they will spawn above it.");
        bellCooldown = getValidatedInt(SETTINGS + ".bell_cooldown", 10);
        bellWorkAfter = getValidatedInt(SETTINGS + ".bell_work_after", 60);
        height = getValidatedInt(SETTINGS + ".height", 10);
        radius = getValidatedDouble(SETTINGS + ".radius", 50);
        teleportDelay = getValidatedInt(SETTINGS + ".delay", 60);
    }

    /**
     * Loads raid check settings.
     */
    private void loadRaidCheckSettings() {
        raidCheckMode = getRaidCheckMode(RAID_CHECK + ".mode", "SCHEDULER");
        worldCheckFrequency = getValidatedInt(RAID_CHECK + ".world_frequency", 100);
        maxChecksPerTick = getValidatedInt(RAID_CHECK + ".max_checks_per_tick", 5);
        raidCheckFrequency = getValidatedInt(RAID_CHECK + ".raid_frequency", 20);
    }

    /**
     * Loads and validates the list of worlds from the
     * configuration.
     */
    private void loadWorldList() {
        final List<String> worldNames = fileConfig.getStringList(WORLDS);
        worldList = new ArrayList<>(
                worldNames.stream()
                        .map(Bukkit::getWorld)
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    // 2. Utility Methods

    /**
     * Returns a Component for the provided config path,
     * or a default value.
     *
     * @param path Configuration path.
     * @param defaultValue Default string.
     * @return A Component representing the message.
     */
    private Component getComponent(final String path, final String defaultValue) {
        final String value = fileConfig.getString(path, defaultValue);
        return Component.text(value);
    }

    /**
     * Retrieves the raid check mode from the config,
     * defaults to the provided value if invalid.
     *
     * @param path Configuration path.
     * @param defaultValue Default mode value.
     * @return A RaidCheckMode value.
     */
    private RaidCheckMode getRaidCheckMode(final String path, final String defaultValue) {
        final String mode = fileConfig.getString(path, defaultValue).toUpperCase();
        try {
            return RaidCheckMode.valueOf(mode);
        } catch (final IllegalArgumentException e) {
            logger.warning("Invalid raid check mode at '" + path + "'. Defaulting to " + defaultValue);
            return RaidCheckMode.valueOf(defaultValue.toUpperCase());
        }
    }

    // 3. Validation Methods

    /**
     * Returns a validated non-negative integer from the config, or the default value if invalid.
     *
     * @param path Configuration path.
     * @param defaultValue Default value.
     * @return A valid non-negative integer.
     */
    private int getValidatedInt(final String path, final int defaultValue) {
        final int value = fileConfig.getInt(path, defaultValue);
        if (value < 0) {
            logger.warning("Config value at '" + path + "' cannot be negative. Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    /**
     * Returns a validated non-negative double from the config, or the default value if invalid.
     *
     * @param path Configuration path.
     * @param defaultValue Default value.
     * @return A valid non-negative double.
     */
    private double getValidatedDouble(final String path, final double defaultValue) {
        final double value = fileConfig.getDouble(path, defaultValue);
        if (value < 0) {
            logger.warning("Config value at '" + path + "' cannot be negative. Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    // 4. Error Handling

    /**
     * Disables the plugin due to critical configuration errors.
     */
    private void disablePlugin() {
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }
}

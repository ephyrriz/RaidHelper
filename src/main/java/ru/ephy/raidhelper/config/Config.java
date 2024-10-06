package ru.ephy.raidhelper.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Config class handles loading and validating plugin
 * configuration values.
 */
@Getter
@RequiredArgsConstructor
public class Config {

    private static final String MESSAGES = "settings.messages";     // Messages section
    private static final String MECHANICS = "settings.mechanics";   // Mechanics section
    private static final String RAID_CHECK = "settings.raid_check"; // Raid check section
    private static final String WORLDS = "settings.worlds";         // Worlds section

    private final JavaPlugin plugin;             // Plugin's instance
    private final FileConfiguration fileConfig;  // Plugin's configuration file
    private final Logger logger;                 // Logger for debugging

    private RaidCheckMode raidCheckMode;         // Active raid check mode
    private Set<World> worldSet;                 // Valid worlds from the configuration
    private Component ringMessage;               // Action bar message
    private Component cooldownMessage;           // Cooldown message
    private Component someInCooldownMessage;     // Some raids cooldown message
    private double radius;                       // Bell teleportation radius
    private int height;                          // Height for raider teleportation
    private int poolMaxSize;                     // Maximum size for Teleporter pool
    private int bellCooldown;                    // Bell cooldown duration
    private int bellWorkDelay;                   // Delay before bell activation
    private int worldCheckFrequency;             // Frequency for world checks
    private int maxChecksPerTick;                // Max checks per tick
    private int teleportDelay;                   // Delay before raiders teleport

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
    public void loadConfig() {
        loadMessagesSeetings();
        loadMechanicsSettings();
        loadRaidCheckSettings();
        loadWorldList();

        if (worldSet.isEmpty()) {
            logger.severe("No valid worlds found in config. Disabling the plugin.");
            disablePlugin();
        }
    }

    /**
     * Loads the messages settings for the plugin.
     */
    private void loadMessagesSeetings() {
        ringMessage = getComponent(MESSAGES + ".ring",
                "If you can't find the raiders, just ring a bell and they will spawn above it.");
        cooldownMessage = getComponent(MESSAGES + ".cooldown",
                "Hey, not that quick! Wait a few seconds more before you can teleport raiders again");
        someInCooldownMessage = getComponent(MESSAGES + ".some_cooldown",
                "Some of the raids are in cooldown, but those that work have teleported raiders");
    }

    /**
     * Loads the mechanics settings for the plugin.
     */
    private void loadMechanicsSettings() {
        bellCooldown = getValidatedInt(MECHANICS + ".bell_cooldown", 100);
        bellWorkDelay = getValidatedInt(MECHANICS + ".bell_work_delay", 60);
        teleportDelay = getValidatedInt(MECHANICS + ".delay", 60);
        height = getValidatedInt(MECHANICS + ".height", 10);
        radius = getValidatedDouble(MECHANICS + ".radius", 50);
    }

    /**
     * Loads raid check settings.
     */
    private void loadRaidCheckSettings() {
        raidCheckMode = getRaidCheckMode(RAID_CHECK + ".mode", "SCHEDULER");
        worldCheckFrequency = getValidatedInt(RAID_CHECK + ".world_frequency", 100);
        maxChecksPerTick = getValidatedInt(RAID_CHECK + ".max_checks_per_tick", 5);
        poolMaxSize = getValidatedInt(RAID_CHECK + ".max_pool_size", 5);
    }

    /**
     * Loads and validates the list of worlds from the
     * configuration.
     */
    private void loadWorldList() {
        final List<String> worldNames = fileConfig.getStringList(WORLDS);
        worldSet = new HashSet<>();

        for (final String worldName : worldNames) {
            final World world = Bukkit.getWorld(worldName);

            if (world != null) {
                worldSet.add(world);
            }
        }
    }

    /**
     * Returns a Component for the provided config path,
     * or a default value.
     *
     * @param path          Configuration path
     * @param defaultValue  Default string
     * @return A Component representing the message
     */
    private Component getComponent(final String path, final String defaultValue) {
        final String value = fileConfig.getString(path, defaultValue);
        return Component.text(value);
    }

    /**
     * Retrieves the raid check mode from the config,
     * defaults to the provided value if invalid.
     *
     * @param path          Configuration path
     * @param defaultValue  Default mode value
     * @return A RaidCheckMode value
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

    /**
     * Returns a validated non-negative integer from the
     * config, or the default value if invalid.
     *
     * @param path          Configuration path
     * @param defaultValue  Default value
     * @return A valid non-negative integer
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
     * Returns a validated non-negative double from the
     * config, or the default value if invalid.
     *
     * @param path          Configuration path
     * @param defaultValue  Default value
     * @return A valid non-negative double
     */
    private double getValidatedDouble(final String path, final double defaultValue) {
        final double value = fileConfig.getDouble(path, defaultValue);
        if (value < 0) {
            logger.warning("Config value at '" + path + "' cannot be negative. Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    /**
     * Disables the plugin if called.
     */
    private void disablePlugin() {
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }
}

package ru.ephy.raidhelper.config;

import lombok.Getter;
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
 * Handles the plugin's configuration by loading and validating
 * values from the configuration file. If critical configuration
 * (like worlds) is missing, the plugin will be disabled.
 */
@Getter
public class Config {

    private static final String MESSAGES = "settings.messages";     // Path to messages section
    private static final String MECHANICS = "settings.mechanics";   // Path to mechanics section
    private static final String RAID_CHECK = "settings.raid_check"; // Path to raid check section
    private static final String WORLDS = "settings.worlds";         // Path to worlds section

    private final JavaPlugin plugin;             // Plugin instance
    private final FileConfiguration configFile;  // Configuration file instance
    private final Logger logger;                 // Logger instance

    private RaidCheckMode raidCheckMode;         // Raid check mode (SCHEDULER or EVENT)
    private Set<World> validWorlds;              // Set of valid worlds from the configuration
    private Component teleportMessage;           // Message when teleport raiders
    private Component ringMessage;               // Message when ringing the bell is avaliable
    private Component cooldownWarning;           // Message for cooldown warning
    private Component partialCooldownWarning;    // Some raids cooldown message
    private double radius;                       // Radius for teleportation around the bell
    private int height;                          // Teleportation height
    private int maxPoolSize;                     // Maximum size of the teleporter pool
    private int bellCooldown;                    // Bell cooldown duration
    private int bellWorkDelay;                   // Delay before bell activation
    private int worldCheckFrequency;             // Frequency of world checks in ticks
    private int maxChecksPerTick;                // Maximum raid checks per tick
    private int teleportDelay;                   // Delay before teleporting raiders
    private int cacheExpireTime;                 // Cache expire time

    /**
     * Enum representing the raid check modes.
     */
    public enum RaidCheckMode {
        SCHEDULER,
        EVENT
    }

    /**
     * Constructor that loads and validates the configuration values.
     * Disables the plugin if the world list is empty.
     *
     * @param plugin      Plugin instance
     * @param configFile  Configuration file instance
     * @param logger      Logger instance for debugging
     */
    public Config(final JavaPlugin plugin, final FileConfiguration configFile,
                  final Logger logger) {
        // Initialize required instances
        this.plugin = plugin;
        this.configFile = configFile;
        this.logger = logger;

        // Load config values
        loadMessageSettings();
        loadMechanicsSettings();
        loadRaidCheckSettings();
        loadValidWorlds();

        // Disable plugin if no valid worlds are found
        if (validWorlds.isEmpty()) {
            logger.severe("No valid worlds found in config. Disabling the plugin.");
            disablePlugin();
        }
    }

    /**
     * Loads messages from the configuration file.
     * Sets default messages if not found in the file.
     */
    private void loadMessageSettings() {
        teleportMessage = loadComponent(MESSAGES + ".teleport",
                "Raiders on their way to your bell!");
        ringMessage = loadComponent(MESSAGES + ".ring",
                "If you can't find the raiders, just ring the bell");
        cooldownWarning = loadComponent(MESSAGES + ".cooldown",
                "Please wait before ringing the bell again.");
        partialCooldownWarning = loadComponent(MESSAGES + ".some_cooldown",
                "Some raids are still in cooldown, but others are active. Teleporting available raiders.");
    }

    /**
     * Loads mechanics-related settings like cooldowns, delays,
     * teleportation height, and radius from the config.
     */
    private void loadMechanicsSettings() {
        bellCooldown = getValidatedInt(MECHANICS + ".bell_cooldown", 100);
        bellWorkDelay = getValidatedInt(MECHANICS + ".bell_work_delay", 60);
        teleportDelay = getValidatedInt(MECHANICS + ".teleport_delay", 60);
        height = getValidatedInt(MECHANICS + ".spawn_height", 10);
        radius = getValidatedDouble(MECHANICS + ".effect_radius", 50);
    }

    /**
     * Loads raid check-related settings like check frequency
     * and pool size from the config.
     */
    private void loadRaidCheckSettings() {
        raidCheckMode = getRaidCheckMode(RAID_CHECK + ".mode", "SCHEDULER");
        worldCheckFrequency = getValidatedInt(RAID_CHECK + ".world_frequency", 100);
        maxChecksPerTick = getValidatedInt(RAID_CHECK + ".max_checks_per_tick", 5);
        maxPoolSize = getValidatedInt(RAID_CHECK + ".max_pool_size", 5);
        cacheExpireTime = getValidatedInt(RAID_CHECK + ".cache_expire_time", 200);
    }

    /**
     * Loads and validates the worlds listed in the configuration.
     * Only valid worlds (non-NETHER because there are no raids)
     * are added to the set.
     */
    private void loadValidWorlds() {
        final List<String> worldNames = configFile.getStringList(WORLDS);
        validWorlds = new HashSet<>();

        for (final String worldName : worldNames) {
            final World world = Bukkit.getWorld(worldName);

            if (world != null && world.getEnvironment() != World.Environment.NETHER) {
                validWorlds.add(world);
            } else {
                logger.warning("The world " + worldName + " is null or a nether world.");
            }
        }
    }

    /**
     * Returns a text component from the config, or the default message
     * if the path is not found. Uses caching for frequently accessed
     * configuration paths.
     *
     * @param path         Configuration path
     * @param defaultValue Default message value
     * @return Text component for the message
     */
    private Component loadComponent(final String path, final String defaultValue) {
        return Component.text(configFile.getString(path, defaultValue));
    }

    /**
     * Returns the raid check mode from the configuration file.
     * Defaults to SCHEDULER if the mode is invalid.
     *
     * @param path          Configuration path
     * @param defaultValue  Default value if invalid
     * @return RaidCheckMode value
     */
    private RaidCheckMode getRaidCheckMode(final String path, final String defaultValue) {
        final String mode = configFile.getString(path, defaultValue).toUpperCase();
        try {
            return RaidCheckMode.valueOf(mode);
        } catch (final IllegalArgumentException exception) {
            logger.warning("Invalid raid check mode at '" + path + "'. Defaulting to " + defaultValue);
            return RaidCheckMode.valueOf(defaultValue.toUpperCase());
        }
    }

    /**
     * Retrieves a validated non-negative integer from the config.
     * Returns the default value if the config value is negative or invalid.
     *
     * @param path          Configuration path
     * @param defaultValue  Default value
     * @return Validated integer value
     */
    private int getValidatedInt(final String path, final int defaultValue) {
        final int value = configFile.getInt(path, defaultValue);
        if (value < 0) {
            logger.warning("Config value at '" + path + "' cannot be negative. Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    /**
     * Retrieves a validated non-negative double from the config.
     * Returns the default value if the config value is negative or invalid.
     *
     * @param path          Configuration path
     * @param defaultValue  Default value
     * @return Validated double value
     */
    private double getValidatedDouble(final String path, final double defaultValue) {
        final double value = configFile.getDouble(path, defaultValue);
        if (value < 0) {
            logger.warning("Config value at '" + path + "' cannot be negative. Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    /**
     * Disables the plugin if was called.
     */
    private void disablePlugin() {
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }
}

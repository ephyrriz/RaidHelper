package ru.ephy.raidhelper;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.raid.data.RaidManager;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.monitor.RaidEventMonitor;
import ru.ephy.raidhelper.raid.monitor.RaidSchedulerMonitor;
import ru.ephy.raidhelper.raid.scheduler.RaidScheduler;
import ru.ephy.raidhelper.raid.events.bell.BellRing;
import ru.ephy.raidhelper.raid.events.end.RaidEnd;

import java.util.logging.Logger;

/**
 * Main class for the RaidHelper plugin.
 * Initializes essential components, manages
 * lifecycle events, and starts raid monitoring.
 */
public final class Raidhelper extends JavaPlugin {

    private PluginManager pluginManager; // Plugin manager
    private RaidManager raidManager;     // Manages active raids
    private Config config;               // Holds plugin configuration settings
    private Logger logger;               // Logger for debugging

    /**
     * Called when the plugin is enabled by the server.
     * Initializes components and registers event listeners.
     */
    @Override
    public void onEnable() {
        initializeCoreComponents();
        startRaidSystems();
        registerListeners();
    }

    /**
     * Initializes core components like logger, config,
     * plugin manager, and raid manager.
     */
    private void initializeCoreComponents() {
        logger = getLogger();
        config = initializeConfig();
        pluginManager = getServer().getPluginManager();
        raidManager = new RaidManager(logger);
    }

    /**
     * Initializes configuration and loads config values.
     *
     * @return A Config object with loaded settings
     */
    private Config initializeConfig() {
        saveDefaultConfig();
        config = new Config(this, getConfig(), logger);
        config.loadValues();
        return config;
    }

    /**
     * Starts the appropriate raid monitoring and scheduling
     * systems based on configuration.
     */
    private void startRaidSystems() {
        startRaidMonitor();
        startRaidScheduler();
    }

    /**
     * Starts the raid monitoring system based on the
     * chosen by a user mode.
     */
    private void startRaidMonitor() {
        switch (config.getRaidCheckMode()) {
            case SCHEDULER -> new RaidSchedulerMonitor(this, raidManager, config).startMonitor();
            case EVENT -> pluginManager.registerEvents(new RaidEventMonitor(this, raidManager, config), this);
            default -> {
                logger.warning("Invalid RaidCheckMode. Defaulting to SCHEDULER.");
                new RaidSchedulerMonitor(this, raidManager, config).startMonitor();
            }
        }
    }

    /**
     * Starts the raid scheduling system to periodically manage active raids.
     */
    private void startRaidScheduler() {
        new RaidScheduler(this, raidManager, config, logger).startScheduler();
    }

    /**
     * Registers event listeners for bell interactions and raid end events.
     */
    private void registerListeners() {
        final BellRing bellRing = new BellRing(this, raidManager, config, logger);
        final RaidEnd raidEnd = new RaidEnd(raidManager);

        pluginManager.registerEvents(bellRing, this);
        pluginManager.registerEvents(raidEnd, this);
    }

    /**
     * Called when the plugin is disabled by the server.
     * Handles cleanup tasks before shutdown.
     */
    @Override
    public void onDisable() {
        // Add any cleanup or shutdown logic here if needed in the future.
    }
}

package ru.ephy.raidhelper;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.raid.data.RaidManager;
import ru.ephy.raidhelper.config.Config;
import ru.ephy.raidhelper.raid.monitor.RaidMonitor;
import ru.ephy.raidhelper.raid.scheduler.RaidScheduler;
import ru.ephy.raidhelper.raid.events.BellListener;
import ru.ephy.raidhelper.raid.events.RaidEndListener;

import java.util.logging.Logger;

/**
 * The main class for the RaidHelper plugin.
 * This class initializes necessary components and manages
 * the lifecycle events of the plugin such as enabling and disabling.
 */
public final class Raidhelper extends JavaPlugin {

    private RaidManager raidManager;  // Manages active raids
    private Config config;            // Holds plugin configuration settings
    private Logger logger;            // Logger for debugging

    /**
     * Called when the plugin is enabled by the server.
     * Initializes variables, loads configuration, and registers event listeners.
     */
    @Override
    public void onEnable() {
        initializeComponents();
        registerListeners();
    }

    /**
     * Initializes essential components such as the configuration,
     * raid manager, raid monitor, and scheduler.
     */
    private void initializeComponents() {
        initializeLogger();
        initializeConfig();
        initializeRaidManager();

        startRaidMonitor();
        startRaidScheduler();
    }

    /**
     * Initializes logger.
     */
    private void initializeLogger() {
        logger = getLogger();
    }

    /**
     * Initializes config.
     */
    private void initializeConfig() {
        saveDefaultConfig();
        config = new Config(this, getConfig(), logger);
        config.loadValues();
    }

    /**
     * Initializes Raid Manager.
     */
    private void initializeRaidManager() {
        raidManager = new RaidManager(this, config, logger);
    }

    /**
     * Starts the raid monitoring system.
     */
    private void startRaidMonitor() {
        final RaidMonitor raidMonitor = new RaidMonitor(this, raidManager, config);
        raidMonitor.startMonitor();
    }

    /**
     * Starts the raid scheduling system.
     */
    private void startRaidScheduler() {
        final RaidScheduler raidScheduler = new RaidScheduler(this, raidManager, config, logger);
        raidScheduler.startScheduler();
    }

    /**
     * Registers all event listeners used by the plugin.
     * This includes listeners for bell ring and raid finish events.
     */
    private void registerListeners() {
        final PluginManager pluginManager = getServer().getPluginManager();

        final BellListener bellListener = new BellListener(this, raidManager, config, logger);
        bellListener.initializeVariables();
        final RaidEndListener raidEndListener = new RaidEndListener(raidManager);

        pluginManager.registerEvents(bellListener, this);
        pluginManager.registerEvents(raidEndListener, this);
    }

    /**
     * Called when the plugin is disabled by the server.
     * Handles any cleanup or saving necessary before shutdown.
     */
    @Override
    public void onDisable() {
        // If there are tasks or resources that need to be cleaned up when the plugin shuts down,
        // this is where you would add that logic.
    }
}

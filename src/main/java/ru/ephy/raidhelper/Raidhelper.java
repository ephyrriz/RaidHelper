package ru.ephy.raidhelper;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.raids.RaidManager;
import ru.ephy.raidhelper.files.Config;
import ru.ephy.raidhelper.raids.RaidScheduler;
import ru.ephy.raidhelper.raids.events.BellRingEventListener;
import ru.ephy.raidhelper.raids.events.RaidFinishEventListener;
import ru.ephy.raidhelper.raids.events.RaidSpawnWaveEventListener;

import java.util.logging.Logger;

/**
 * The main class for the RaidHelper plugin.
 * This class initializes all necessary instances and
 * settings before enabling the plugin.
 */
public final class Raidhelper extends JavaPlugin {
    private RaidManager raidManager;  // RaidManager instance reference
    private Config config;            // Holds configuration data
    private Logger logger;            // Logger for logging information

    /**
     * The logic executed when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        initializeVariables();
        registerListeners();
        logger.info("Plugin enabled.");
    }

    /**
     * Initializes the necessary variables and components
     * for the plugin.
     */
    private void initializeVariables() {
        logger = getLogger();
        saveDefaultConfig();

        // Load configuration settings
        config = new Config(this, getConfig(), logger);
        config.loadValues();

        // Initialize raid manager and scheduler
        raidManager = new RaidManager(this, config, logger);
        final RaidScheduler raidScheduler = new RaidScheduler(this, raidManager, config, logger);
        raidScheduler.startScheduler();
    }

    /**
     * Registers the necessary listeners.
     */
    private void registerListeners() {
        final PluginManager pluginManager = getServer().getPluginManager();

        // Initialize listeners
        final BellRingEventListener bellRingEventListener = new BellRingEventListener
                (this, raidManager, config);
        final RaidFinishEventListener raidFinishEventListener = new RaidFinishEventListener
                (raidManager);
        final RaidSpawnWaveEventListener raidSpawnWaveEventListener = new RaidSpawnWaveEventListener
                (raidManager);

        // Register listeners
        pluginManager.registerEvents(bellRingEventListener, this);
        pluginManager.registerEvents(raidFinishEventListener, this);
        pluginManager.registerEvents(raidSpawnWaveEventListener, this);
    }

    /**
     * The logic executed when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        logger.info("Plugin disabled.");
    }
}

package ru.ephy.raidhelper;

import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.event.RaidManager;
import ru.ephy.raidhelper.files.Config;
import ru.ephy.raidhelper.event.RaidScheduler;

import java.util.logging.Logger;

/**
 * The main class for the RaidHelper plugin.
 * This class initializes all necessary instances and
 * settings before enabling the plugin.
 */
public final class Raidhelper extends JavaPlugin {
    private Logger logger;

    /**
     * The logic executed when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        initializeVariables();
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
        final Config config = new Config(this, getConfig(), logger);
        config.loadValues();

        // Initialize raid manager and scheduler
        final RaidManager raidManager = new RaidManager();
        final RaidScheduler raidScheduler = new RaidScheduler(this, raidManager, config, logger);
        raidScheduler.startScheduler();
    }

    /**
     * The logic executed when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        logger.info("Plugin disabled.");
    }
}

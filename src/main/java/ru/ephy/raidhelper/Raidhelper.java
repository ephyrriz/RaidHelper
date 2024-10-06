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
 * Handles initialization, lifecycle management,
 * and raid monitoring systems.
 */
public final class Raidhelper extends JavaPlugin {

    private JavaPlugin plugin;           // Plugin reference
    private PluginManager pluginManager; // Bukkit plugin manager
    private RaidManager raidManager;     // Raid management system
    private Config config;               // Plugin configuration
    private Logger logger;               // Plugin logger

    /**
     * Called when the plugin is enabled.
     * Initializes components and starts raid systems.
     */
    @Override
    public void onEnable() {
        initializeCoreComponents();
        startRaidSystems();
        registerListeners();
    }

    /**
     * Initializes core components like the logger, config,
     * plugin manager, and raid manager.
     */
    private void initializeCoreComponents() {
        plugin = this;
        logger = getLogger();
        config = initializeConfig();
        pluginManager = getServer().getPluginManager();
        raidManager = new RaidManager(logger);
    }

    /**
     * Loads and returns the configuration settings.
     *
     * @return Config object containing plugin settings
     */
    private Config initializeConfig() {
        saveDefaultConfig();
        return new Config(plugin, getConfig(), logger);
    }

    /**
     * Starts raid monitoring and scheduling systems based on
     * configuration.
     */
    private void startRaidSystems() {
        startRaidMonitor();
        startRaidScheduler();
    }

    /**
     * Starts the appropriate raid monitoring system
     * based on the selected mode (Scheduler/Event).
     */
    private void startRaidMonitor() {
        switch (config.getRaidCheckMode()) {
            case SCHEDULER -> new RaidSchedulerMonitor(plugin, raidManager, config, logger);
            case EVENT -> pluginManager.registerEvents(new RaidEventMonitor(plugin, raidManager, config), plugin);
            default -> {
                logger.warning("Invalid RaidCheckMode. Defaulting to SCHEDULER.");
                new RaidSchedulerMonitor(plugin, raidManager, config, logger);
            }
        }
    }

    /**
     * Starts a scheduler to periodically manage raids.
     */
    private void startRaidScheduler() {
        new RaidScheduler(plugin, raidManager, config, logger);
    }

    /**
     * Registers event listeners for raid-related events.
     */
    private void registerListeners() {
        final BellRing bellRing = new BellRing(plugin, raidManager, config, logger);
        final RaidEnd raidEnd = new RaidEnd(raidManager);

        pluginManager.registerEvents(bellRing, plugin);
        pluginManager.registerEvents(raidEnd, plugin);
    }

    /**
     * Called when the plugin is disabled. Reserved for cleanup tasks.
     */
    @Override
    public void onDisable() {
        // Handle any necessary cleanup tasks if needed in the future.
    }
}

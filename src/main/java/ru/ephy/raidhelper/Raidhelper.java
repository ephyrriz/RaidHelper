package ru.ephy.raidhelper;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.files.Config;
import ru.ephy.raidhelper.raid_events.RaidEventsListener;
import ru.ephy.raidhelper.raid_events.RaidScheduler;
import ru.ephy.raidhelper.raid_events.bell_event.BellRingListener;
import ru.ephy.raidhelper.raid_events.raid_management.RaidManager;

import java.util.logging.Logger;

/**
 * This class is the main one. It initializes all necessary
 * variables and instances before booting the rest parts
 * of the itself.
 */
public final class Raidhelper extends JavaPlugin {
    private Logger logger;

    /**
     * The logic of the plugin when enabling.
     */
    @Override
    public void onEnable() {
        registerListeners();
        initializeVariables();
        logger.info("Plugin enabled.");
    }

    private void registerListeners() {
        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new RaidEventsListener(this), this);
        pluginManager.registerEvents(new BellRingListener(this), this);
    }

    private void initializeVariables() {
        logger = getLogger();

        saveDefaultConfig();
        final Config config = new Config(getConfig());
        config.initialize();

        final RaidManager raidManager = new RaidManager();

        new RaidScheduler(this, raidManager, config, logger).startScheduler();
    }

    /**
     * The logic of the plugin when disabling.
     */
    @Override
    public void onDisable() {
        logger.info("Plugin disabled.");
    }
}

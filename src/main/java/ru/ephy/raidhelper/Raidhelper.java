package ru.ephy.raidhelper;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.configuration.Config;
import ru.ephy.raidhelper.raid_events.RaidEventsListener;
import ru.ephy.raidhelper.raid_events.RaidScheduler;
import ru.ephy.raidhelper.raid_events.bell_event.BellRingListener;

public final class Raidhelper extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Config.load(getConfig());

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new RaidEventsListener(this), this);
        pluginManager.registerEvents(new BellRingListener(this), this);

        new RaidScheduler(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

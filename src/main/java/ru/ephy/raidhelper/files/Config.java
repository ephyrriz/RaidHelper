package ru.ephy.raidhelper.files;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * This class retrieves the values from the config to use
 * them in the rest part of the plugin.
 */
@Getter
@RequiredArgsConstructor
public class Config {

    private static final String MAIN_SETTINGS = "settings.main";
    private static final String ADVANCED_SETTINGS = "settings.advanced";

    private final JavaPlugin plugin;            // Plugin's instance.
    private final FileConfiguration fileConfig; // File config.
    private final Logger logger;                // Logger.

    private List<World> worldList;              // Worlds list.
    private String message;                     // Action bar message that is shown for players.
    private int height;                         // How far above raiders will be teleported.
    private int radius;                         // The radious of the bell's effect.
    private int cooldown;                       // After how many ticks will work.
    private int frequencyWorld;                 // How often check for the raids.
    private int frequencyRaid;                  // How often check the current time of the raid.
    private int delay;                          // After how many ticks teleport the raiders.

    /**
     * initailizes the file config and loads necessary values.
     */
    public void loadValues() {
        try {
            // Main settings
            message = fileConfig.getString(MAIN_SETTINGS + ".message", "If you can't find the raiders, just ring a bell and they will spawn above it.");
            height = fileConfig.getInt(MAIN_SETTINGS + ".height", 10);
            radius = fileConfig.getInt(MAIN_SETTINGS + ".radius", 50);
            cooldown = fileConfig.getInt(MAIN_SETTINGS + ".cooldown", 60);
            delay = fileConfig.getInt(MAIN_SETTINGS + ".delay", 60);
            // Advanced settings
            frequencyWorld = fileConfig.getInt(ADVANCED_SETTINGS + ".frequencyWorld", 100);
            frequencyRaid = fileConfig.getInt(ADVANCED_SETTINGS + ".frequencyRaid", 20);
            worldList = loadWorldList();
        } catch (final Exception e) {
            logger.severe("An unexpected error occured during loading the values from the config: " + e.getMessage());
            e.printStackTrace();
            disablePlugin();
        }

        if (worldList.isEmpty()) {
            logger.severe("No valid worlds found. Turning the plugin off due to its unnecessity.");
            disablePlugin();
        }
    }

    /**
     * This method is used to iterate through the worlds names
     * and turn them into real worlds, if they exist.
     *
     * @return the valid world list.
     */
    @NotNull
    private List<World> loadWorldList() {
        final List<String> worldNames = fileConfig.getStringList("settings.worlds");
        return new ArrayList<>(
                worldNames.stream()
                        .map(Bukkit::getWorld)
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    /**
     * Turns off the plugin.
     */
    private void disablePlugin() {
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }
}

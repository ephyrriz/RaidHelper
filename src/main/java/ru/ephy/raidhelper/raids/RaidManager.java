package ru.ephy.raidhelper.raids;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Raid;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ephy.raidhelper.files.Config;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class RaidManager {

    private final JavaPlugin plugin;
    private final Config config;

    private final Map<Raid, RaidLogic> raidMap = new HashMap<>();

    public void addRaid(final Raid raid) {
        final RaidLogic raidLogic = new RaidLogic(plugin, config, raid);
        raidLogic.startCounter();
        raidMap.put(raid, raidLogic);
    }

    public void removeRaid(final Raid raid) {
        raidMap.remove(raid);
    }

    public boolean isRaidInMap(final Raid raid) {
        return raidMap.containsKey(raid);
    }
}

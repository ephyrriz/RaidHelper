package ru.ephy.raidhelper.raids;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Raid;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
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
        final RaidLogic raidLogic = getRaidLogic(raid);
        if (raidLogic != null) {
            raidLogic.stopCounter();
            raidMap.remove(raid);
        }
    }

    @Nullable
    public RaidLogic getRaidLogic(final Raid raid) {
        final RaidLogic raidLogic = raidMap.get(raid);
        if (raidLogic != null) {
            return raidMap.get(raid);
        }
        return null;
    }

    public boolean isRaidInMap(final Raid raid) {
        return raidMap.containsKey(raid);
    }
}

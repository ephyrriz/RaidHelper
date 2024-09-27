package ru.ephy.raidhelper.event;

import lombok.Getter;
import org.bukkit.Raid;

import java.util.HashMap;
import java.util.Map;

@Getter
public class RaidManager {
    final Map<Raid, RaidData> raidMap = new HashMap<>();

    public void addRaid() {}
    public void getRaid() {}
    public void removeRaid() {}
}

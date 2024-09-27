package ru.ephy.raidhelper.event;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Raid;

@RequiredArgsConstructor
public class RaidData {
    private final long activeTicks;
    private final Location location;
    private final Raid raid;
}

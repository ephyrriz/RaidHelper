package ru.ephy.raidhelper.raid_events.raid_management;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Raid;

@Getter
@RequiredArgsConstructor
public class RaidData {
    private final Raid raid;
    private final RaidTimeCounter raidTimeCounter;
}

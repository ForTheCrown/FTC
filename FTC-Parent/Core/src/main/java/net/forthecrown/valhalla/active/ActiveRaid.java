package net.forthecrown.valhalla.active;

import net.forthecrown.utils.math.FtcRegion;
import org.bukkit.Location;

public class ActiveRaid {

    private final Location start;
    private final FtcRegion region;

    public ActiveRaid(Location start, FtcRegion region) {
        this.start = start;
        this.region = region;
    }

    public FtcRegion getRegion() {
        return region;
    }

    public Location getStart() {
        return start;
    }
}

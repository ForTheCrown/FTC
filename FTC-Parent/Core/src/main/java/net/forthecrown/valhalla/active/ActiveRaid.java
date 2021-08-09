package net.forthecrown.valhalla.active;

import net.forthecrown.utils.math.FtcRegion;
import net.forthecrown.valhalla.RaidDifficulty;
import net.forthecrown.valhalla.Valhalla;
import org.bukkit.Location;

public class ActiveRaid {

    private final Location start;
    private final FtcRegion region;
    private final RaidDifficulty difficulty;

    public ActiveRaid(Location start, FtcRegion region, RaidDifficulty difficulty) {
        this.start = start;
        this.region = region;
        this.difficulty = difficulty;

        Valhalla.getInstance().setActiveRaid(this);
    }

    public void start() {

    }

    public void shutDown() {

    }

    public RaidDifficulty getDifficulty() {
        return difficulty;
    }

    public FtcRegion getRegion() {
        return region;
    }

    public Location getStart() {
        return start;
    }
}

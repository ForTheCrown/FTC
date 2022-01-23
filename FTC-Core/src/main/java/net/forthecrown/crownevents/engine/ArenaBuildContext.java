package net.forthecrown.crownevents.engine;

import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.Location;

public class ArenaBuildContext {
    private final Location buildLocation;
    private final CrownRandom random = new CrownRandom();

    public ArenaBuildContext(Location buildLocation) {
        this.buildLocation = buildLocation;
    }

    public Location getBuildLocation() {
        return buildLocation;
    }

    public WorldVec3i getBuildPosition() {
        return WorldVec3i.of(getBuildLocation());
    }

    public CrownRandom getRandom() {
        return random;
    }
}

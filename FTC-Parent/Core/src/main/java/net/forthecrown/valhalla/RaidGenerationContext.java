package net.forthecrown.valhalla;

import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.math.FtcRegion;
import net.forthecrown.valhalla.data.VikingRaid;
import net.minecraft.core.Position;
import org.bukkit.Location;
import org.bukkit.World;

public class RaidGenerationContext {

    private final World world;
    private final VikingRaid raid;
    private final CrownRandom random;

    private final Location startingLocation;
    private final FtcRegion region;

    public RaidGenerationContext(World world, VikingRaid raid, CrownRandom random) {
        this.world = world;
        this.raid = raid;
        this.random = random;

        Position pos = raid.getStartingPos();
        this.startingLocation = new Location(world, pos.x(), pos.y(), pos.z());

        this.region = FtcRegion.of(raid.getRegion(), world);
    }

    public World getWorld() {
        return world;
    }

    public VikingRaid getRaid() {
        return raid;
    }

    public FtcRegion getRegion() {
        return region.clone();
    }

    public Location getStartingLocation() {
        return startingLocation.clone();
    }

    public CrownRandom getRandom() {
        return random;
    }
}

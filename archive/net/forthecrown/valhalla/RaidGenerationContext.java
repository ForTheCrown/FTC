package net.forthecrown.valhalla;

import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.forthecrown.valhalla.data.VikingRaid;
import net.minecraft.core.Position;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;

public class RaidGenerationContext {

    private final World world;
    private final VikingRaid raid;
    private final CrownRandom random;
    private final RaidDifficulty difficulty;
    private final Location startingLocation;
    private final FtcBoundingBox region;
    private final Collection<Player> players;

    public RaidGenerationContext(World world,
                                 VikingRaid raid,
                                 CrownRandom random,
                                 RaidDifficulty difficulty,
                                 Collection<Player> players
    ) {
        this.world = world;
        this.raid = raid;
        this.random = random;
        this.difficulty = difficulty;
        this.players = players;

        Position pos = raid.getStartingPos();
        this.startingLocation = new Location(world, pos.x(), pos.y(), pos.z());

        this.region = FtcBoundingBox.of(world, raid.getRegion());
    }

    public World getWorld() {
        return world;
    }

    public VikingRaid getRaid() {
        return raid;
    }

    public FtcBoundingBox getRegion() {
        return region.clone();
    }

    public Location getStartingLocation() {
        return startingLocation.clone();
    }

    public CrownRandom getRandom() {
        return random;
    }

    public RaidDifficulty getDifficulty() {
        return difficulty;
    }

    public Collection<Player> getPlayers() {
        return players;
    }
}

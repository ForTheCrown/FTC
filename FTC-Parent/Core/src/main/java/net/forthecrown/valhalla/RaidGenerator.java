package net.forthecrown.valhalla;

import net.forthecrown.squire.Squire;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.Worlds;
import net.forthecrown.valhalla.active.ActiveRaid;
import net.forthecrown.valhalla.data.VikingRaid;
import org.apache.commons.lang.Validate;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;

public class RaidGenerator {

    private final VikingRaid raid;
    private final World world;
    private final CrownRandom random;

    private final RaidGenerationContext context;

    public RaidGenerator(VikingRaid raid) {
        this.raid = raid;
        this.random = new CrownRandom();
        this.world = createWorld();

        context = new RaidGenerationContext(world, raid, random);
    }

    public ActiveRaid generate() {
        Validate.isTrue(!Valhalla.getInstance().activeRaidExists(), "Active Raid already exists");

        if(getRaid().hasLootData()) getRaid().getLootData().generate(getContext());

        return new ActiveRaid(context.getStartingLocation(), context.getRegion());
    }

    private World createWorld() {
        World result = new WorldCreator(Squire.createVikingsKey("world_raids_actual"))
                .copy(Worlds.RAIDS)
                .createWorld();

        File exampleDir = Worlds.RAIDS.getWorldFolder();
        File resultDir = result.getWorldFolder();



        return result;
    }

    public CrownRandom getRandom() {
        return random;
    }

    public World getWorld() {
        return world;
    }

    public VikingRaid getRaid() {
        return raid;
    }

    public RaidGenerationContext getContext() {
        return context;
    }
}

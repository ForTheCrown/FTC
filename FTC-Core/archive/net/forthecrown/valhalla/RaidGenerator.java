package net.forthecrown.valhalla;

import net.forthecrown.squire.Squire;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.Worlds;
import net.forthecrown.valhalla.active.ActiveRaid;
import net.forthecrown.valhalla.data.VikingRaid;
import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class RaidGenerator {
    public static final NamespacedKey WORLD_KEY = Squire.createVikingsKey("world_raids_actual");

    private final VikingRaid raid;
    private final World world;
    private final CrownRandom random;
    private final Collection<Player> players;
    private final RaidDifficulty difficulty;

    private final RaidGenerationContext context;

    public RaidGenerator(VikingRaid raid) {
        this.raid = raid;
        this.random = new CrownRandom();
        this.world = createWorld();
        this.players = VikingUtil.getGatheredVikings();
        this.difficulty = makeDifficulty(players);

        context = new RaidGenerationContext(world, raid, random, difficulty, players);
    }

    public ActiveRaid generate() {
        Validate.isTrue(!Valhalla.getInstance().activeRaidExists(), "Active Raid already exists");

        if(raid.hasLootData()) raid.getLootData().generate(getContext());

        return new ActiveRaid(context.getStartingLocation(), context.getRegion(), difficulty);
    }

    private RaidDifficulty makeDifficulty(Collection<Player> players) {
        return null;
    }

    private World createWorld() {
        World result = new WorldCreator(WORLD_KEY)
                .copy(Worlds.RAIDS)
                .createWorld();

        File exampleDir = Worlds.RAIDS.getWorldFolder();
        File resultDir = result.getWorldFolder();

        try {
            FileUtils.copyDirectory(exampleDir, resultDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public Collection<Player> getPlayers() {
        return players;
    }

    public RaidDifficulty getDifficulty() {
        return difficulty;
    }
}

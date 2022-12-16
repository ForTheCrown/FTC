package net.forthecrown.utils.world;

import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.io.PathUtil;
import net.kyori.adventure.util.TriState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;

import java.nio.file.Path;

public class WorldReCreator {
    public static WorldReCreator of(World world) {
        return new WorldReCreator(world);
    }

    // The original world
    private final World world;

    // The position the world border's centre is
    // located at
    private final Vec2 worldBorderPos;

    // The size of the original world border
    private final double worldBorderSize;

    // The game rules of the original world
    private final GameRules rules;

    // The original world's name
    private final String name;

    // The seed to use for generation
    private long seed;

    private BiomeProvider biomeProvider;
    private ChunkGenerator generator;
    private boolean preserveWorldBorder;
    private boolean preserveSeed;
    private boolean preserveGameRules;
    private boolean seedSet;

    private WorldReCreator(World world) {
        // Copy basic info
        this.world = world;
        this.seed = world.getSeed();
        this.name = world.getName();

        ServerLevel level = VanillaAccess.getLevel(world);

        // Copy world border info
        WorldBorder border = level.getWorldBorder();
        this.worldBorderPos = new Vec2((float) border.getCenterX(), (float) border.getCenterZ());
        this.worldBorderSize = border.getSize();

        // Create a copy of the game rules
        this.rules = level.getGameRules().copy();
    }

    public static void kickPlayers(World w) {
        Location hazel = GeneralConfig.getServerSpawn();

        for (Player p : w.getPlayers()) {
            p.teleport(hazel);
        }
    }

    public WorldReCreator preserveWorldBorder(boolean preserveWorldBorder) {
        this.preserveWorldBorder = preserveWorldBorder;
        return this;
    }

    public WorldReCreator preserveSeed(boolean keepSeed) {
        this.preserveSeed = keepSeed;
        return this;
    }

    public WorldReCreator generator(ChunkGenerator generator) {
        this.generator = generator;
        return this;
    }

    public WorldReCreator biomeProvider(BiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
        return this;
    }

    public WorldReCreator preserveGameRules(boolean preserveGameRules) {
        this.preserveGameRules = preserveGameRules;
        return this;
    }

    public WorldReCreator seed(long seed) {
        this.seedSet = true;
        this.seed = seed;

        return this;
    }

    public World run() {
        WorldCreator creator = new WorldCreator(name)
                .copy(world)
                .keepSpawnLoaded(TriState.byBoolean(world.getKeepSpawnInMemory()));

        // If we have generator, use it
        if(generator != null) {
            creator.generator(generator);
        }

        // If we have a custom biome provider, use it
        if(biomeProvider != null) {
            creator.biomeProvider(biomeProvider);
        }

        // If we have a seed we can preserve or
        // a set seed, set it
        if(preserveSeed || seedSet) {
            creator.seed(seed);
        } else {
            // Else use a random seed
            creator.seed(Util.RANDOM.nextLong());
        }

        // Get the directory and then unload it
        Path f = world.getWorldFolder().toPath();

        kickPlayers(world);
        Bukkit.unloadWorld(world, true);

        // Delete the original world directory
        var deletionResult = PathUtil.safeDelete(f, true, true);

        if (deletionResult.error().isPresent()) {
            throw new IllegalStateException(
                    "Couldn't delete previous world directory: "
                            + deletionResult.error().get().message()
            );
        }

        // Create the world
        World world = creator.createWorld();
        ServerLevel level = VanillaAccess.getLevel(world);

        // Apply world border if we have one
        // to apply
        if (preserveWorldBorder) {
             WorldBorder border = level.getWorldBorder();

             border.setCenter(worldBorderPos.x, worldBorderPos.y);
             border.setSize(worldBorderSize);
        }

        // If we have to preserve game rules, set them
        if(preserveGameRules) {
            level.getGameRules().assignFrom(rules, level);
        }

        return world;
    }
}
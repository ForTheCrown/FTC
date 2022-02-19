package net.forthecrown.utils.world;

import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.FtcUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.io.IOException;

public class WorldReCreator {
    public static WorldReCreator of(World world) {
        return new WorldReCreator(world);
    }

    private final World world;
    private final Vec2 worldBorderPos;
    private final double worldBorderSize;
    private final GameRules rules;
    private final String name;

    private long seed;
    private BiomeProvider biomeProvider;
    private ChunkGenerator generator;
    private boolean preserveWorldBorder;
    private boolean preserveSeed;
    private boolean preserveGameRules;
    private boolean seedSet;

    private WorldReCreator(World world) {
        this.world = world;
        this.seed = world.getSeed();
        this.name = world.getName();

        ServerLevel level = VanillaAccess.getLevel(world);

        WorldBorder border = level.getWorldBorder();
        this.worldBorderPos = new Vec2((float) border.getCenterX(), (float) border.getCenterZ());
        this.worldBorderSize = border.getSize();

        this.rules = level.getGameRules().copy();
    }

    public boolean preserveWorldBorder() {
        return preserveWorldBorder;
    }

    public WorldReCreator preserveWorldBorder(boolean preserveWorldBorder) {
        this.preserveWorldBorder = preserveWorldBorder;
        return this;
    }

    public boolean preserveSeed() {
        return preserveSeed;
    }

    public WorldReCreator preserveSeed(boolean keepSeed) {
        this.preserveSeed = keepSeed;
        return this;
    }

    public ChunkGenerator generator() {
        return generator;
    }

    public WorldReCreator generator(ChunkGenerator generator) {
        this.generator = generator;
        return this;
    }

    public BiomeProvider biomeProvider() {
        return biomeProvider;
    }

    public WorldReCreator biomeProvider(BiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
        return this;
    }

    public boolean preserveGameRules() {
        return preserveGameRules;
    }

    public WorldReCreator preserveGameRules(boolean preserveGameRules) {
        this.preserveGameRules = preserveGameRules;
        return this;
    }

    public boolean isSeedSet() {
        return seedSet;
    }

    public WorldReCreator seed(long seed) {
        this.seedSet = true;
        this.seed = seed;

        return this;
    }

    public World run() {
        WorldCreator creator = new WorldCreator(name)
                .copy(world);

        if(generator != null) creator.generator(generator);
        if(biomeProvider != null) creator.biomeProvider(biomeProvider);

        if(preserveSeed || seedSet) creator.seed(seed);
        else creator.seed(FtcUtils.RANDOM.nextLong());

        File f = world.getWorldFolder();
        Bukkit.unloadWorld(world, true);

        try {
            FileUtils.deleteDirectory(f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        World world = creator.createWorld();
        ServerLevel level = VanillaAccess.getLevel(world);

        if(preserveWorldBorder) {
             WorldBorder border = level.getWorldBorder();

             border.setCenter(worldBorderPos.x, worldBorderPos.y);
             border.setSize(worldBorderSize);
        }

        if(preserveGameRules) {
            level.getGameRules().assignFrom(rules, MinecraftServer.getServer());
        }

        return world;
    }
}

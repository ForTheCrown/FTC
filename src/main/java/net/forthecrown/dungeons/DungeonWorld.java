package net.forthecrown.dungeons;

import net.forthecrown.utils.world.WorldReCreator;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public final class DungeonWorld {
    private DungeonWorld() {}

    public static final String WORLD_NAME = "world_dungeons";

    public static World get() {
        return Bukkit.getWorld(WORLD_NAME);
    }

    public static World reset() {
        World world = get();

        if (world == null) {
            return new WorldCreator(WORLD_NAME)
                    .generator("VoidGen")
                    .type(WorldType.FLAT)
                    .keepSpawnLoaded(TriState.FALSE)
                    .environment(World.Environment.NORMAL)
                    .createWorld();
        }

        return WorldReCreator.of(world)
                .preserveGameRules(true)
                .preserveWorldBorder(true)
                .preserveSeed(true)
                .run();
    }
}
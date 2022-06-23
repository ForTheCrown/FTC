package net.forthecrown.core;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;

import java.util.EnumSet;

// Terrible attempt at recreating biome-categories.
// Should be used in ResourceWorld class, but isn't.
//
// 'Terrible' You got that right lmao
// Note to Wout:
// Making yourself write new HashSet(Arrays.asList()) for each enum constant
// is a weird form of sadomasochism lmao. Just put a Biome... in the constructor
// parameter and then do a new HashSet(Arrays.aslist(biomes)); inside the constructor,
// saves u a lot of work lol
// Remember, don't repeat yourself unless there is literally no other option :D
public enum BiomeCategory {
    NONE,
    TAIGA,
    EXTREME_HILLS,
    JUNGLE,
    MESA,
    PLAINS,
    SAVANNA,
    ICY,
    THEEND,
    BEACH,
    FOREST,
    OCEAN,
    DESERT,
    RIVER,
    SWAMP,
    MUSHROOM,
    NETHER,
    UNDERGROUND,
    MOUNTAIN;

    private final EnumSet<Biome> biomes = EnumSet.noneOf(Biome.class);
    private static final BiomeCategory[] VALUES;

    static {
        populateCategories();
        VALUES = values();
    }

    public boolean contains(Holder<net.minecraft.world.level.biome.Biome> biomeHolder) {
        return biomes.contains(CraftBlock.biomeBaseToBiome(biomeRegistry(), biomeHolder));
    }

    public boolean contains(Biome biome) {
        return biomes.contains(biome);
    }

    public static BiomeCategory get(Holder<net.minecraft.world.level.biome.Biome> biomeHolder) {
        return get(CraftBlock.biomeBaseToBiome(biomeRegistry(), biomeHolder));
    }

    public static BiomeCategory get(Biome b) {
        for (BiomeCategory c: VALUES) {
            if (c.contains(b)) return c;
        }

        return NONE;
    }

    private static Registry<net.minecraft.world.level.biome.Biome> biomeRegistry() {
        return DedicatedServer.getServer().registryHolder.registryOrThrow(Registry.BIOME_REGISTRY);
    }

    private static void populateCategories() {
        // Pre 1.19 biomes
        p(OCEAN, Biome.OCEAN);
        p(PLAINS, Biome.PLAINS);
        p(DESERT, Biome.DESERT);
        p(EXTREME_HILLS, Biome.WINDSWEPT_HILLS);
        p(FOREST, Biome.FOREST);
        p(TAIGA, Biome.TAIGA);
        p(SWAMP, Biome.SWAMP);
        p(RIVER, Biome.RIVER);
        p(NETHER, Biome.NETHER_WASTES);
        p(THEEND, Biome.THE_END);
        p(OCEAN, Biome.FROZEN_OCEAN);
        p(RIVER, Biome.FROZEN_RIVER);
        p(ICY, Biome.SNOWY_PLAINS);
        p(MUSHROOM, Biome.MUSHROOM_FIELDS);
        p(BEACH, Biome.BEACH);
        p(JUNGLE, Biome.JUNGLE);
        p(JUNGLE, Biome.SPARSE_JUNGLE);
        p(OCEAN, Biome.DEEP_OCEAN);
        p(BEACH, Biome.STONY_SHORE);
        p(BEACH, Biome.SNOWY_BEACH);
        p(FOREST, Biome.BIRCH_FOREST);
        p(FOREST, Biome.DARK_FOREST);
        p(TAIGA, Biome.SNOWY_TAIGA);
        p(TAIGA, Biome.OLD_GROWTH_PINE_TAIGA);
        p(EXTREME_HILLS, Biome.WINDSWEPT_FOREST);
        p(SAVANNA, Biome.SAVANNA);
        p(SAVANNA, Biome.SAVANNA_PLATEAU);
        p(MESA, Biome.BADLANDS);
        p(MESA, Biome.WOODED_BADLANDS);
        p(THEEND, Biome.SMALL_END_ISLANDS);
        p(THEEND, Biome.END_MIDLANDS);
        p(THEEND, Biome.END_HIGHLANDS);
        p(THEEND, Biome.END_BARRENS);
        p(OCEAN, Biome.WARM_OCEAN);
        p(OCEAN, Biome.LUKEWARM_OCEAN);
        p(OCEAN, Biome.COLD_OCEAN);
        p(OCEAN, Biome.DEEP_LUKEWARM_OCEAN);
        p(OCEAN, Biome.DEEP_COLD_OCEAN);
        p(OCEAN, Biome.DEEP_FROZEN_OCEAN);
        p(NONE, Biome.THE_VOID);
        p(PLAINS, Biome.SUNFLOWER_PLAINS);
        p(EXTREME_HILLS, Biome.WINDSWEPT_GRAVELLY_HILLS);
        p(FOREST, Biome.FLOWER_FOREST);
        p(ICY, Biome.ICE_SPIKES);
        p(FOREST, Biome.OLD_GROWTH_BIRCH_FOREST);
        p(TAIGA, Biome.OLD_GROWTH_SPRUCE_TAIGA);
        p(SAVANNA, Biome.WINDSWEPT_SAVANNA);
        p(MESA, Biome.ERODED_BADLANDS);
        p(JUNGLE, Biome.BAMBOO_JUNGLE);
        p(NETHER, Biome.SOUL_SAND_VALLEY);
        p(NETHER, Biome.CRIMSON_FOREST);
        p(NETHER, Biome.WARPED_FOREST);
        p(NETHER, Biome.BASALT_DELTAS);
        p(UNDERGROUND, Biome.DRIPSTONE_CAVES);
        p(UNDERGROUND, Biome.LUSH_CAVES);
        p(MOUNTAIN, Biome.MEADOW);
        p(FOREST, Biome.GROVE);
        p(MOUNTAIN, Biome.SNOWY_SLOPES);
        p(MOUNTAIN, Biome.FROZEN_PEAKS);
        p(MOUNTAIN, Biome.JAGGED_PEAKS);
        p(MOUNTAIN, Biome.STONY_PEAKS);

        // 1.19 biomes
        p(SWAMP, Biome.MANGROVE_SWAMP);
        p(UNDERGROUND, Biome.DEEP_DARK);
    }

    private static void p(BiomeCategory category, Biome biome) {
        category.biomes.add(biome);
    }
}
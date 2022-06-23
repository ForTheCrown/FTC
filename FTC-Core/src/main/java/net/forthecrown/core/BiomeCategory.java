package net.forthecrown.core;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Terrible attempt at recreating biome-categories.
// Should be used in ResourceWorld class, but isn't.
public enum BiomeCategory {

    PLAINS(new HashSet<>(Arrays.asList(
            Biomes.PLAINS,
            Biomes.SUNFLOWER_PLAINS,
            Biomes.SNOWY_PLAINS
    ))),
    DESERT(new HashSet<>(Arrays.asList(
            Biomes.DESERT
    ))),
    FOREST(new HashSet<>(Arrays.asList(
            Biomes.FOREST,
            Biomes.FLOWER_FOREST,
            Biomes.BIRCH_FOREST,
            Biomes.DARK_FOREST,
            Biomes.OLD_GROWTH_BIRCH_FOREST,
            Biomes.WINDSWEPT_FOREST
    ))),
    MESA(new HashSet<>(Arrays.asList(
            Biomes.BADLANDS,
            Biomes.ERODED_BADLANDS,
            Biomes.WOODED_BADLANDS
    ))),
    TAIGA(new HashSet<>(Arrays.asList(
            Biomes.TAIGA,
            Biomes.SNOWY_TAIGA,
            Biomes.OLD_GROWTH_PINE_TAIGA,
            Biomes.OLD_GROWTH_SPRUCE_TAIGA
    ))),
    JUNGLE(new HashSet<>(Arrays.asList(
            Biomes.JUNGLE,
            Biomes.BAMBOO_JUNGLE,
            Biomes.SPARSE_JUNGLE
    ))),
    SAVANNA(new HashSet<>(Arrays.asList(
            Biomes.SAVANNA,
            Biomes.SAVANNA_PLATEAU,
            Biomes.WINDSWEPT_SAVANNA
    ))),
    MOUNTAIN(new HashSet<>(Arrays.asList(
            Biomes.FROZEN_PEAKS,
            Biomes.JAGGED_PEAKS,
            Biomes.STONY_PEAKS
    ))),
    CAVES(new HashSet<>(Arrays.asList(
            Biomes.DEEP_DARK,
            Biomes.LUSH_CAVES
    ))),
    ;

    public static BiomeCategory getBiomeCategory(Biome b) {
        for(BiomeCategory bc : BiomeCategory.values()) {
            if (bc.biomes.contains(b)) return bc;
        }
        return null;
    }

    public Set<ResourceKey<Biome>> biomes;

    BiomeCategory(Set<ResourceKey<Biome>> biomes) {
        this.biomes = biomes;
    }


}

package net.forthecrown.dungeons.level;

import com.google.common.collect.ImmutableList;
import java.util.Random;
import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.PerlinNoiseGenerator;
import org.spongepowered.math.vector.Vector3d;

public class BiomeSource {
  private final ImmutableList<LevelBiome> biomes;
  private final NoiseGenerator generator;

  public BiomeSource(Random random, LevelBiome[] biomes) {
    this.generator = new PerlinNoiseGenerator(random);
    this.biomes = ImmutableList.copyOf(biomes);
  }

  public LevelBiome findBiome(Vector3d vec) {
    return findBiome(vec.x(), vec.y(), vec.z());
  }

  public LevelBiome findBiome(double x, double y, double z) {
    if (biomes.isEmpty()) {
      return LevelBiome.NORMAL;
    }

    LevelBiome res = null;
    double highest = 0.0D;

    for (int i = 0; i < biomes.size(); i++) {
      LevelBiome biome = biomes.get(i);
      double noise = generator.noise(x, y, z, 1, 0.25D, i, true);

      if (res == null || noise > highest) {
        res = biome;
        highest = noise;
      }
    }

    assert res != null;
    return res;
  }


}
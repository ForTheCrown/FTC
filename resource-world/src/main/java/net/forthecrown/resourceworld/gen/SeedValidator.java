package net.forthecrown.resourceworld.gen;

import net.forthecrown.resourceworld.RwConfig;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;

abstract class SeedValidator {

  Seed seed;
  RandomState randomState;
  NoiseBasedChunkGenerator gen;
  RwConfig config;
  GenConfig params;

  int baseY;
  int worldSize;

  public final void bindFinder(SeedFinder finder) {
    if (finder == null) {
      config = null;
      params = null;

      return;
    }

    this.config = finder.getConfig();
    this.params = finder.getParams();
  }

  public final void bindSeed(Seed seed, int baseY, int worldSize) {
    this.baseY = baseY;
    this.worldSize = worldSize;

    if (seed == null) {
      this.seed = null;
      randomState = null;
      gen = null;

      return;
    }

    this.seed = seed;
    this.randomState = seed.getRandomState();
    this.gen = seed.getGen();
  }

  public abstract void evaluate();
}

package net.forthecrown.resourceworld.gen;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
class Seed implements Comparable<Seed> {

  final long seed;

  int score;
  boolean failed;

  final NoiseBasedChunkGenerator gen;
  final RandomState randomState;

  public Seed(long seed) {
    this.seed = seed;

    var registryAccess = VanillaAccess.getServer().registryAccess();
    WorldDimensions dimensions = WorldPresets.createNormalWorldDimensions(registryAccess);

    gen = (NoiseBasedChunkGenerator) dimensions.overworld();
    randomState = RandomState.create(
        gen.generatorSettings().value(),
        registryAccess.lookupOrThrow(Registries.NOISE),
        seed
    );
  }

  @Override
  public int compareTo(@NotNull Seed o) {
    if (o.failed == this.failed) {
      return Integer.compare(o.score, this.score);
    }

    return this.failed ? 1 : -1;
  }
}

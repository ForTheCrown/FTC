package net.forthecrown.resourceworld.gen;

import static net.forthecrown.resourceworld.Constants.HEIGHT_ACCESSOR;
import static net.forthecrown.resourceworld.Constants.HEIGHT_MAP_TYPE;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;

class SpawnValidator extends SeedValidator {

  @Override
  public void evaluate() {
    if (baseY > params.getMaxSpawnY()) {
      seed.failed = true;
      return;
    } else {
      seed.score++;
    }

    int spawnCheckQuart = params.getSpawnCheckRadius() >> 2;

    for (int x = -spawnCheckQuart; x < spawnCheckQuart; x++) {
      for (int z = -spawnCheckQuart; z < spawnCheckQuart; z++) {
        if (isAreaGood(x, z, baseY)) {
          continue;
        }

        seed.failed = true;
        return;
      }
    }
    seed.score++;
  }

  private boolean isAreaGood(int x, int z, int baseY) {
    int blockX = QuartPos.toBlock(x);
    int blockZ = QuartPos.toBlock(z);

    var biomes = gen.getBiomeSource();

    // Get the difference between this area's
    // Y level and the base Y
    int y = gen.getBaseHeight(blockX, blockZ, HEIGHT_MAP_TYPE, HEIGHT_ACCESSOR, randomState);
    int dif = baseY - y;

    int maxDif = params.getMaxYDif();

    if (dif > maxDif || dif < -maxDif) {
      return false;
    }

    if (params.getSpawnBiomes().isEmpty()) {
      return true;
    }

    Holder<Biome> b = biomes.getNoiseBiome(x, QuartPos.fromBlock(y), z, randomState.sampler());

    return b.unwrapKey()
        .map(params.getSpawnBiomes()::contains)
        .orElse(false);
  }
}

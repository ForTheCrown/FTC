package net.forthecrown.resourceworld.gen;

import static net.forthecrown.resourceworld.gen.SeedFinder.LOGGER;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Kind;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class BiomeValidator extends SeedValidator {

  @Override
  public void evaluate() {
    int halfSize = worldSize / 2;

    int max = QuartPos.fromBlock( halfSize);
    int min = QuartPos.fromBlock(-halfSize);

    int skipover = params.getBiomeCheckSkipover();

    var biomeSource = seed.gen.getBiomeSource();
    var sampler = seed.randomState.sampler();

    Object2IntMap<Holder<Biome>> occurs = new Object2IntOpenHashMap<>();

    Set<RequiredRange<TagKey<Biome>>> requiredTags
        = new ObjectOpenHashSet<>(params.getRequiredBiomeTags());

    Set<RequiredRange<ResourceKey<Biome>>> requiredBiomes
        = new ObjectOpenHashSet<>(params.getRequiredBiomes());

    for (int x = min; x < max; x += skipover) {
      for (int z = min; z < max; z += skipover) {
        Holder<Biome> biome = biomeSource.getNoiseBiome(x, baseY, z, sampler);

        if (biome.kind() == Kind.DIRECT) {
          LOGGER.warn(
              "Found direct holder while looking for " +
                  "biomes, cannot access tags"
          );

          continue;
        }

        int count = occurs.computeInt(
            biome,
            (biomeHolder, integer) -> integer == null ? 1 : integer + 1
        );

        Iterator<RequiredRange<ResourceKey<Biome>>> reqIt = requiredBiomes.iterator();
        while (reqIt.hasNext()) {
          var req = reqIt.next();
          if (!biome.is(req.getValue())) {
            continue;
          }

          if (!req.contains(count)) {
            continue;
          }

          seed.score++;
          reqIt.remove();
        }

        Iterator<RequiredRange<TagKey<Biome>>> tagIt = requiredTags.iterator();
        while (tagIt.hasNext()) {
          var tag = tagIt.next();

          if (!biome.is(tag.getValue())) {
            continue;
          }

          if (!tag.contains(count)) {
            continue;
          }

          seed.score++;
          tagIt.remove();
        }

        if (requiredBiomes.isEmpty() && requiredTags.isEmpty()) {
          return;
        }
      }
    }

    seed.failed = true;
  }
}

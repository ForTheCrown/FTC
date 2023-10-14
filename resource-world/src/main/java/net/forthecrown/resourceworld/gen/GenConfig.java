package net.forthecrown.resourceworld.gen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.bukkit.HeightMap;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftNamespacedKey;

@Getter
public class GenConfig {

  private int spawnCheckRadius;
  private int maxSpawnY;
  private int maxYDif;
  private int findAttempts;
  private int finderThreads;
  private int biomeCheckSkipover;

  private boolean biomeCheckDisabled;
  private boolean invalidSeedsAllowed;
  private boolean structureCheckDisabled;
  private boolean spawnCheckDisabled;

  private Set<ResourceKey<Biome>> spawnBiomes = Set.of();
  private Set<RequiredRange<TagKey<Biome>>> requiredBiomeTags = Set.of();
  private Set<RequiredRange<ResourceKey<Biome>>> requiredBiomes = Set.of();
  private Set<RequiredRange<ResourceKey<Structure>>> requiredStructures = Set.of();

  private HeightMap heightMap = HeightMap.OCEAN_FLOOR_WG;

  public static Result<GenConfig> load(JsonWrapper json) {
    GenConfig params = new GenConfig();
    params.spawnCheckRadius = json.getInt("spawnCheckRadius", 20);
    params.maxSpawnY = json.getInt("maxSpawnY", 75);
    params.maxYDif = json.getInt("maxYDif", 2);
    params.findAttempts = json.getInt("findAttempts", 1024);
    params.finderThreads = json.getInt("finderThreads", 3);
    params.biomeCheckSkipover = json.getInt("biomeCheckSkipover", 8);

    params.biomeCheckDisabled = json.getBool("disabledBiomeCheck", false);
    params.invalidSeedsAllowed = json.getBool("allowInvalidSeeds", false);
    params.structureCheckDisabled = json.getBool("skipStructureCheck", false);
    params.spawnCheckDisabled = json.getBool("skipSpawnCheck", false);

    if (params.biomeCheckSkipover < 1) {
      return Result.error("biomeCheckSkipover < 1");
    }

    if (params.finderThreads < 1) {
      return Result.error("finderThreads < 1");
    }

    if (params.spawnCheckRadius < 1) {
      return Result.error("spawnCheckRadius < 1");
    }

    if (params.maxYDif < 1) {
      return Result.error("maxYDif < 1");
    }

    if (params.findAttempts < 1) {
      return Result.error("findAttempts < 1");
    }

    try {
      params.heightMap = json.getEnum("heightMap", HeightMap.class, HeightMap.OCEAN_FLOOR_WG);
    } catch (IllegalArgumentException exc) {
      return Result.error("Invalid heightMap value: '%s'", json.get("heightMap"));
    }

    if (json.has("spawnBiomes")) {
      params.spawnBiomes = json.getList("spawnBiomes", JsonUtils::readKey)
          .stream()
          .map(CraftNamespacedKey::toMinecraft)
          .map(resourceLocation -> ResourceKey.create(Registries.BIOME, resourceLocation))
          .collect(Collectors.toUnmodifiableSet());
    }

    if (json.has("requiredBiomeTags")) {
      var setResult = readRangeSet(json, "requiredBiomeTags", "tag", element -> {
        return TagKey.create(Registries.BIOME, element);
      });

      if (setResult.isError()) {
        return setResult.cast();
      }

      params.requiredBiomeTags = setResult.getValue();
    }

    if (json.has("requiredBiomes")) {
      var setResult = readRangeSet(json, "requiredBiomes", "biome", loc -> {
        return ResourceKey.create(Registries.BIOME, loc);
      });

      if (setResult.isError()) {
        return setResult.cast();
      }

      params.requiredBiomes = setResult.getValue();
    }

    if (json.has("requiredStructures")) {
      var setResult = readRangeSet(json, "requiredStructures", "struct", loc -> {
        return ResourceKey.create(Registries.STRUCTURE, loc);
      });

      if (setResult.isError()) {
        return setResult.cast();
      }

      params.requiredStructures = setResult.getValue();
    }

    return Result.success(params);
  }

  private static <T> Result<Set<RequiredRange<T>>> readRangeSet(
      JsonWrapper wrapper,
      String key,
      String valueKey,
      Function<ResourceLocation, T> reader
  ) {
    JsonElement element = wrapper.get(key);

    if (!element.isJsonArray()) {
      return toLocation(element)
          .map(reader)
          .map(t -> new RequiredRange<>(t, null, null))
          .map(Set::of);
    }

    JsonArray arr = element.getAsJsonArray();
    Result<Set<RequiredRange<T>>> setResult = Result.success(new ObjectOpenHashSet<>());

    for (int i = 0; i < arr.size(); i++) {
      JsonElement el = arr.get(i);
      final int fI = i;

      Integer min = null;
      Integer max = null;

      Result<T> res;

      if (el.isJsonObject()) {
        JsonWrapper json = JsonWrapper.wrap(el.getAsJsonObject());

        if (!json.has(valueKey)) {
          res = Result.error("Missing value element '%s'", key);
        } else {
          res = toLocation(json.get(key)).map(reader);
        }

        if (json.has("min")) {
          min = json.getInt("min");
        }

        if (json.has("max")) {
          max = json.getInt("max");
        }
      } else {
        res = toLocation(el).map(reader);
      }

      res = res.mapError(s -> "Error reading element[" + fI + "]: " + s);

      final Integer fMin = min;
      final Integer fMax = max;

      setResult = setResult.combine(
          res,
          (s, s2) -> s + "\n" + s2,
          (t, requiredRanges) -> {
            requiredRanges.add(new RequiredRange<>(t, fMin, fMax));
            return requiredRanges;
          }
      );
    }

    return setResult
        .map(Collections::unmodifiableSet)
        .mapError(s -> "Error reading " + key + ":\n" + s);
  }

  private static Result<ResourceLocation> toLocation(JsonElement element) {
    try {
      var key = JsonUtils.readKey(element);
      return Result.success(CraftNamespacedKey.toMinecraft(key));
    } catch (IllegalArgumentException exc) {
      return Result.error("Invalid key: %s", element);
    }
  }
}

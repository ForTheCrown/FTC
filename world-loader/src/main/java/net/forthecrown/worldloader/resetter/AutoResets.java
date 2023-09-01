package net.forthecrown.worldloader.resetter;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.utils.io.Results;
import net.forthecrown.worldloader.SeedGenerator;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataType;

public final class AutoResets {
  private AutoResets() {}

  public static final NamespacedKey PDC_KEY = new NamespacedKey("automated_reset", "last_reset");
  private static final String GEN_BY = "generated_by:";

  public static Instant getLastReset(World world) {
    var pdc = world.getPersistentDataContainer();

    if (!pdc.has(PDC_KEY, PersistentDataType.LONG)) {
      return null;
    }

    long time = pdc.get(PDC_KEY, PersistentDataType.LONG);
    return Instant.ofEpochMilli(time);
  }

  public static void setLastReset(World world, Instant reset) {
    var pdc = world.getPersistentDataContainer();
    pdc.set(PDC_KEY, PersistentDataType.LONG, reset.toEpochMilli());
  }

  static DataResult<SeedGenerator> loadSeedGenerator(
      JsonElement element,
      SeedGenerator fallback
  ) {
    if (element == null || element.isJsonNull()) {
      return Results.error("Null value");
    }

    if (element.isJsonArray()) {
      return Codec.LONG_STREAM.decode(JsonOps.INSTANCE, element)
          .map(pair -> pair.getFirst().toArray())
          .flatMap(longs -> {
            if (longs.length < 1) {
              return Results.error("At least 1 seed is required within the seed array");
            }

            return Results.success(new LongArraySeedGenerator(longs, new Random()));
          });
    }

    String str = element.getAsString();
    if (str.startsWith(GEN_BY)) {
      if (fallback == null) {
        return Results.error("'generated_by' value not allowed here");
      }

      String pluginName = str.substring(GEN_BY.length());
      return Results.success(new PluginSeedGenerator(pluginName, fallback));
    }

    if (str.equals("keep") || str.equals("preserve")) {
      return Results.success(SeedGenerator.KEEP);
    }

    if (str.equals("random")) {
      return Results.success(SeedGenerator.RANDOM);
    }

    long l;
    Long seed;

    try {
      l = Long.parseLong(str);
    } catch (NumberFormatException exc) {
      l = str.hashCode();
    }

    seed = l;
    return Results.success(world -> CompletableFuture.completedFuture(seed));
  }

  record LongArraySeedGenerator(long[] seeds, Random random) implements SeedGenerator {

    @Override
    public CompletableFuture<Long> generateSeed(World world) {
      long seed = random.nextInt(seeds.length);
      return CompletableFuture.completedFuture(seed);
    }
  }
}

package net.forthecrown.worldloader.resetter;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import java.time.Duration;
import java.time.Instant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.Results;
import net.forthecrown.worldloader.SeedGenerator;
import net.forthecrown.worldloader.WorldLoaderService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;

@Getter
@RequiredArgsConstructor
public class AutoResettingWorld {

  private static final Logger LOGGER = Loggers.getLogger();

  private final String worldName;

  private final PregenArea pregenArea;
  private final Duration resetInterval;
  private final SeedGenerator seedgen;

  private BukkitTask task;

  public static DataResult<AutoResettingWorld> load(
      String worldName,
      JsonElement element,
      SeedGenerator fallbackGen
  ) {
    if (element == null || !element.isJsonObject()) {
      return Results.error("Not an object");
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    if (!json.has("interval")) {
      return Results.error("No 'interval' set");
    }

    Duration resetInterval = JsonUtils.readDuration(json.get("interval"));

    PregenArea pregenArea;
    SeedGenerator seedgen;

    if (json.has("seed")) {
      var res = AutoResets.loadSeedGenerator(json.get("seed"), fallbackGen)
          .mapError(string -> "Couldn't load seed generator: " + string);

      if (Results.isError(res)) {
        return Results.cast(res);
      }

      seedgen = Results.value(res);
    } else {
      seedgen = SeedGenerator.RANDOM;
    }

    if (json.has("pregen_area")) {
      var result = PregenArea.load(json.get("pregen_area"))
          .mapError(string -> "Couldn't load pregen area: " + string);

      if (result.error().isPresent()) {
        return Results.cast(result);
      }

      pregenArea = result.getOrThrow(false, null);
    } else {
      pregenArea = null;
    }

    return Results.success(new AutoResettingWorld(worldName, pregenArea, resetInterval, seedgen));
  }

  public World getWorld() {
    return Bukkit.getWorld(worldName);
  }

  public void stop() {
    task = Tasks.cancel(task);
  }

  public void schedule() {
    var world = getWorld();

    if (world == null) {
      LOGGER.error("Cannot schedule automated reset for world {}: World not found", worldName);
      return;
    }

    Instant lastReset = AutoResets.getLastReset(world);

    Duration untilReset;

    if (lastReset == null) {
      untilReset = resetInterval;
    } else {
      Instant now = Instant.now();
      Instant nextReset = lastReset.plus(resetInterval);

      if (now.isAfter(nextReset)) {
        regen();
        return;
      }

      untilReset = Duration.between(now, nextReset);
    }

    Tasks.runLater(this::regen, untilReset);
  }

  public void regen() {
    var world = getWorld();

    if (world == null) {
      LOGGER.error("Cannot reset world {}: World not found", worldName);
      return;
    }

    seedgen.generateSeed(world).whenComplete((seed, throwable) -> {
      if (throwable != null) {
        LOGGER.error("Error getting seed for world {}", worldName, throwable);
        return;
      }

      Tasks.runSync(() -> genFromSeedSync(world, seed));
    });
  }

  private void genFromSeedSync(World oldWorld, Long seed) {
    WorldLoaderService service = WorldLoaderService.worldLoader();

    var regenArea = this.pregenArea.createArea(oldWorld);
    var remade = service.remakeWorld(oldWorld, seed);

    if (remade == null) {
      LOGGER.error("Failed to remake world {}: unknown cause", worldName);
      return;
    }

    if (regenArea != null) {
      service
          .loadWorld(remade)
          .area(regenArea.minX(), regenArea.minZ(), regenArea.maxX(), regenArea.maxZ())
          .start();
    }

    var now = Instant.now();
    AutoResets.setLastReset(remade, now);
  }
}

package net.forthecrown.resourceworld;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.Loggers;
import net.forthecrown.Worlds;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.resourceworld.gen.GenConfig;
import net.forthecrown.resourceworld.gen.SeedFinder;
import net.forthecrown.structure.Structures;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.worldloader.WorldLoaderService;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.mcteam.ancientgates.Gate;
import org.mcteam.ancientgates.Gates;
import org.slf4j.Logger;

public class ResourceWorld {

  public static final Logger LOGGER = Loggers.getLogger();

  private final RwPlugin plugin;
  private final Path dataFile;

  private BukkitTask task;

  private boolean seedSearchActive;
  public long lastReset;
  public long lastSeed;

  ResourceWorld(RwPlugin plugin) {
    this.plugin = plugin;
    this.dataFile = PathUtil.pluginPath(plugin, "resource-world.dat");
  }

  /* ------------------------------ METHODS ------------------------------- */

  public void save() {
    SerializationHelper.writeTagFile(dataFile, this::saveTo);
  }

  private void saveTo(CompoundTag tag) {
    tag.putLong("lastReset", lastReset);
    tag.putLong("lastSeed", lastSeed);
  }

  public void load() {
    SerializationHelper.readTagFile(dataFile, this::loadFrom);
  }

  private void loadFrom(CompoundTag tag) {
    this.lastReset = tag.getLong("lastReset");
    this.lastSeed = tag.getLong("lastSeed");
  }

  private RwConfig config() {
    return plugin.getRwConfig();
  }

  public void schedule() {
    ZonedDateTime nextReset = Time.dateTime(lastReset).plus(config().resetInterval);
    ZonedDateTime now = ZonedDateTime.now();

    if (nextReset.isBefore(now)) {
      resetAndLoad();
      return;
    }

    Duration between = Duration.between(now, nextReset);
    task = Tasks.runLater(this::resetAndLoad, between);
  }

  public void resetAndLoad() {
    var config = config();
    String spawnStructure = config.spawnStructure;
    WorldLoaderService loader = WorldLoaderService.worldLoader();

    if (!Structures.get().getRegistry().contains(spawnStructure)) {
      LOGGER.error("Cannot start RW reset, no spawn structure with key '{}' found",
          spawnStructure
      );
      return;
    }

    if (loader.isLoading(Worlds.resource())) {
      LOGGER.warn("Resource world is already loading????");
      return;
    }

    if (seedSearchActive) {
      LOGGER.warn("resetAndLoad called while RW is already looking for a seed");
      return;
    }

    LOGGER.info("Starting RW reset");

    // Attempt to announce closing
    if (config.messages.resetStart == null) {
      LOGGER.warn("resetStart message is null, cannot announce");
    } else {
      ChannelledMessage.announce(config.messages.resetStart);
    }

    World world = Worlds.resource();

    findSeed(world.getWorldBorder().getSize()).whenComplete((seed, throwable) -> {
      if (throwable != null) {
        LOGGER.error("Error while attempting to find seed, cannot open RW", throwable);
        return;
      }

      if (seed == null) {
        LOGGER.error("Failed to find seed to reset RW");
        return;
      }

      if (loader.isLoading(Worlds.resource())) {
        LOGGER.warn("Resource world is already loading????");
        return;
      }

      Tasks.runSync(() -> onSeedFoundSync(seed));
    });
  }

  private void onSeedFoundSync(long seed) {
    setGatesOpen(false);
    lastSeed = seed;

    World original = Worlds.resource();
    WorldLoaderService service = WorldLoaderService.worldLoader();

    // Re-create world
    World newWorld = service.remakeWorld(original, seed);
    service.loadWorld(newWorld).start();

    // Purge RW sections
    plugin.getTracker().reset();
  }

  public void setGatesOpen(boolean open) {
    setOpen(config().toResGate, open, "Cannot {} res -> haz gate, no gate with {} ID found");
    setOpen(config().toHazGate, open, "Cannot {} haz -> res gate, no gate with {} ID found");

    Gate.save();

    String status = open ? "open" : "close";
    LOGGER.info("setGatesOpen set, status: {}", status);
  }

  private void setOpen(String id, boolean open, String format) {
    Gate g = Gate.get(id);

    if (g == null) {
      LOGGER.warn(format, open ? "open" : "close", id);
      return;
    }

    if (open) {
      Gates.open(g);
    } else {
      Gates.close(g);
    }
  }

  // This method in its current state takes a fair amount of power
  // to complete, if we'd run it soley on the main thread we'd end
  // up crashing it, so we run it async to avoid holding up the main
  // thread
  private CompletableFuture<Long> findSeed(double lastWorldSize) {
    seedSearchActive = true;

    var config = config();
    Random random = new Random();

    if (config.legalSeeds != null && config.legalSeeds.length > 0) {
      var legal = config.legalSeeds;
      long seed = legal[random.nextInt(legal.length)];
      seedSearchActive = false;
      return CompletableFuture.completedFuture(seed);
    }

    Path paramsFile = PathUtil.pluginPath("gen-parameters.toml");
    PluginJar.saveResources("gen-parameters.toml", paramsFile);

    DataResult<JsonObject> readResult = SerializationHelper.readTomlAsJson(paramsFile);

    if (readResult.result().isEmpty()) {
      seedSearchActive = false;
      return CompletableFuture.failedFuture(
          new RuntimeException(readResult.error().get().message())
      );
    }

    JsonWrapper json = JsonWrapper.wrap(readResult.result().get());
    Result<GenConfig> configResult = GenConfig.load(json);

    if (configResult.isError()) {
      seedSearchActive = false;
      return CompletableFuture.failedFuture(new RuntimeException(configResult.getError()));
    }

    SeedFinder finder = new SeedFinder(configResult.getValue(), config, (int) lastWorldSize);
    return finder.run();
  }
}

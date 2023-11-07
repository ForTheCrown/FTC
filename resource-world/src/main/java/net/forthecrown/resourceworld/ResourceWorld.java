package net.forthecrown.resourceworld;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.Loggers;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.resourceworld.gen.GenConfig;
import net.forthecrown.resourceworld.gen.SeedFinder;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;
import org.mcteam.ancientgates.Gate;
import org.mcteam.ancientgates.Gates;
import org.slf4j.Logger;

public class ResourceWorld {

  public static final Logger LOGGER = Loggers.getLogger();

  private final RwPlugin plugin;
  private final Path dataFile;

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
  CompletableFuture<Long> findSeed(double lastWorldSize) {
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

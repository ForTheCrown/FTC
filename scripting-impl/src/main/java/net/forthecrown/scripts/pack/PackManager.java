package net.forthecrown.scripts.pack;

import com.google.gson.JsonElement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.scripts.ScriptManager;
import net.forthecrown.scripts.ScriptService;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;
import org.slf4j.Logger;

@Getter
public class PackManager {

  private static final Logger LOGGER = Loggers.getLogger();

  private final Path directory;
  private final ScriptService service;

  private final Registry<ScriptPack> packs = Registries.newRegistry();

  public boolean started = false;

  public PackManager(ScriptManager service, Path directory) {
    this.service = service;
    this.directory = directory;
  }

  public void activate() {
    LOGGER.info("Activating script packs");
    started = true;

    for (Holder<ScriptPack> entry : packs.entries()) {
      activate(entry);
    }
  }

  private boolean activate(Holder<ScriptPack> entry) {
    var result = entry.getValue().activate();

    result.apply(string -> {
      LOGGER.error("Failed to activate script pack '{}': {}", entry.getKey(), string);
    }, unit -> {
      LOGGER.debug("Activated pack '{}', creating exports...", entry.getKey());
      entry.getValue().createExports();
    });

    return !result.isError();
  }

  public void reload() {
    close();

    PluginJar.saveResources("pack-example", directory.resolve("pack-example"));

    PathUtil.iterateDirectory(directory, false, true, this::loadPack)
        .mapError(string -> "Error iterating over script packs directory: " + string)
        .resultOrPartial(LOGGER::error);
  }

  public void loadPack(Path file) {
    String fName = file.getFileName().toString();

    // Don't load the example pack lol
    if (fName.equals("pack-example")) {
      return;
    }

    if (!Files.isDirectory(file)) {
      LOGGER.debug("File in packs folder, '{}', was not a directory", file);
      return;
    }

    Path metaFile = file.resolve("script-pack.toml");

    if (!Files.exists(metaFile)) {
      LOGGER.warn("Cannot load script pack '{}', no 'script-pack.toml' file exists", fName);
      return;
    }

    SerializationHelper.readAsJson(metaFile, json -> {
      loadPackFromMeta(json.getSource(), file, fName);
    });
  }

  public void loadPackFromMeta(JsonElement element, Path directory, String key) {
    if (packs.contains(key)) {
      LOGGER.error("Duplicate definition of script pack '{}'", key);
      return;
    }

    Optional<PackMeta> result = PackLoader.load(element, new LoadContext(directory, this))
        .mapError(s -> "Failed to load script pack '" + key + "': " + s)
        .resultOrPartial(LOGGER::error);

    if (result.isEmpty()) {
      return;
    }

    PackMeta meta = result.get();
    ScriptPack pack = new ScriptPack(meta, service);

    registerPack(key, pack);
  }

  public void registerPack(String key, ScriptPack pack) {
    var holder = packs.register(key, pack);

    if (started && !activate(holder)) {
      packs.remove(key);
      return;
    }

    LOGGER.debug("Loaded script pack named '{}'", key);
  }

  public void close() {
    for (ScriptPack pack : packs) {
      pack.close();
    }
    packs.clear();
  }
}

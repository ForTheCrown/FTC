package net.forthecrown.structure;

import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.core.registry.RegistryListener;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.logging.log4j.Logger;

public final class Structures {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final Structures inst = new Structures();

  @Getter
  private final Registry<BlockStructure> registry = Registries.newRegistry();

  @Getter
  private final Registry<StructurePool> poolRegistry = Registries.newRegistry();

  @Getter
  private final Path directory;
  private final Path pools;

  private boolean deleteRemovedStructures = true;

  public Structures() {
    this.directory = PathUtil.getPluginDirectory("structures");
    this.pools = directory.resolve("pools");

    PathUtil.ensureDirectoryExists(pools)
        .orThrow();

    registry.setListener(RegistryListener.removalListener(holder -> {
      if (!deleteRemovedStructures) {
        return;
      }

      delete(holder);
    }));
  }

  public static Structures get() {
    return inst;
  }

  @OnSave
  public void save() {
    for (var structure : registry.entries()) {
      Path p = getPath(structure);
      SerializationHelper.writeTagFile(p, structure.getValue()::save);
    }
  }

  @OnLoad
  public void load() {
    deleteRemovedStructures = false;
    registry.clear();
    poolRegistry.clear();
    deleteRemovedStructures = true;

    if (!Files.exists(directory)) {
      return;
    }

    PathUtil.findAllFiles(directory, false)
        .resultOrPartial(LOGGER::error)
        .ifPresent(strings -> {
          strings.forEach(s -> {
            if (s.startsWith("pools")) {
              return;
            }

            Path path = directory.resolve(s + ".dat");
            BlockStructure structure = new BlockStructure();

            LOGGER.debug("loading structure '{}'", s);

            if (!SerializationHelper.readTagFile(path, structure::load)) {
              LOGGER.warn("Couldn't load '{}'", s);
              return;
            }

            registry.register(s, structure);
          });
        });

    PathUtil.iterateDirectory(pools, true, true, path -> {
      var relative = pools.relativize(path);
      var key = relative.toString();

      if (!key.endsWith(".json")) {
        return;
      }

      JsonElement element = JsonUtils.readFile(path);

      if (!element.isJsonArray()) {
        LOGGER.error(
            "Cannot read structure pool {} (path={}), file is not a JSON array",
            key, path
        );

        return;
      }

      var pool = StructurePool.load(element.getAsJsonArray());
      poolRegistry.register(key, pool);
    });
  }

  public void delete(Holder<BlockStructure> structure) {
    var path = getPath(structure);

    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Path getPath(Holder<BlockStructure> holder) {
    String strPath = holder.getKey();
    return directory.resolve(strPath + ".dat");
  }
}
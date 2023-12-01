package net.forthecrown.structure;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.command.arguments.RegistryArguments;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.registry.RegistryListener;
import net.forthecrown.structure.pool.StructurePool;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.slf4j.Logger;

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

  @Getter
  private RegistryArguments<BlockStructure> structureArgument;

  public Structures() {
    this.directory = PathUtil.pluginPath();
    this.pools = directory.resolve("pools");

    PathUtil.ensureDirectoryExists(pools);

    registry.setListener(RegistryListener.removalListener(holder -> {
      if (!deleteRemovedStructures) {
        return;
      }

      delete(holder);
    }));

    structureArgument = new RegistryArguments<>(registry, "Structure");
  }

  public static Structures get() {
    return inst;
  }

  public void save() {
    for (var structure : registry.entries()) {
      Path p = getPath(structure);
      SerializationHelper.writeTagFile(p, structure.getValue()::save);
    }
  }

  public void load() {
    deleteRemovedStructures = false;
    registry.clear();
    poolRegistry.clear();
    deleteRemovedStructures = true;

    if (!Files.exists(directory)) {
      return;
    }

    PathUtil.findAllFiles(directory, false).forEach(s -> {
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

      StructurePool.CODEC.parse(JsonOps.INSTANCE, element)
          .mapError(string -> "Failed to load structure pool '" + key + "': " + string)
          .resultOrPartial(LOGGER::error)
          .ifPresent(pool -> poolRegistry.register(key, pool));
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
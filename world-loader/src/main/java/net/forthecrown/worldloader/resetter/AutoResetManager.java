package net.forthecrown.worldloader.resetter;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.forthecrown.Loggers;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.worldloader.SeedGenerator;
import org.slf4j.Logger;

public class AutoResetManager {

  private static final Logger LOGGER = Loggers.getLogger();

  private final Path path;

  private final Map<String, AutoResettingWorld> worlds = new HashMap<>();
  private SeedGenerator pluginSeedFallback;

  public AutoResetManager() {
    this.path = PathUtil.pluginPath("auto_resets.toml");
  }

  public void activateAll() {
    worlds.forEach((string, autoResettingWorld) -> autoResettingWorld.schedule());
  }

  public void clear() {
    worlds.forEach((string, autoResettingWorld) -> autoResettingWorld.stop());
    worlds.clear();
  }

  public void load() {
    PluginJar.saveResources("auto_resets.toml");
    SerializationHelper.readAsJson(path, this::load);
  }

  private void load(JsonWrapper json) {
    clear();

    if (json.has("plugin_seed_fallback")) {
      var elem = json.remove("plugin_seed_fallback");
      var result = AutoResets.loadSeedGenerator(elem, null);

      if (result.result().isEmpty()) {
        LOGGER.error("Couldn't load plugin_seed_fallback: '{}' falling back to KEEP",
            Results.getError(result)
        );

        pluginSeedFallback = SeedGenerator.KEEP;
      } else {
        pluginSeedFallback = result.getOrThrow(false, null);
      }
    } else {
      pluginSeedFallback = SeedGenerator.KEEP;
    }

    for (Entry<String, JsonElement> entry : json.entrySet()) {
      String worldName = entry.getKey();

      DataResult<AutoResettingWorld> result = AutoResettingWorld.load(
          worldName, entry.getValue(), pluginSeedFallback
      );

      if (Results.isError(result)) {
        var mapped = result.mapError(string -> {
          return "Couldn't load auto-resetter for world '" + worldName + "': " + string;
        });

        LOGGER.error(Results.getError(mapped));
        continue;
      }

      LOGGER.debug("Loaded auto-resetter for world {}", worldName);
      this.worlds.put(worldName, Results.value(result));
    }

    activateAll();
  }
}

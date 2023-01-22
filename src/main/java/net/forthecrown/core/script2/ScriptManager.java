package net.forthecrown.core.script2;

import static net.forthecrown.utils.io.FtcJar.ALLOW_OVERWRITE;
import static net.forthecrown.utils.io.FtcJar.OVERWRITE_IF_NEWER;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnDayChange;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.events.Events;
import net.forthecrown.utils.MonthDayPeriod;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.Logger;
import org.bukkit.scheduler.BukkitScheduler;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

@Getter
public class ScriptManager {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final Set<String> ILLEGAL_CLASSES = Set.of(
      BukkitScheduler.class.getName(),
      Tasks.class.getName(),
      Events.class.getName()
  );

  @Getter
  private static final ScriptManager instance = new ScriptManager();

  /**
   * The scripts directory
   */
  private final Path directory;

  /**
   * The JSON file from which scripts are loaded
   */
  private final Path loaderFile;

  /**
   * Factory to create script engines
   */
  private final NashornScriptEngineFactory
      factory = new NashornScriptEngineFactory();

  /**
   * Currently loaded scripts, specified in <code>loader.toml</code>
   */
  private final Registry<LoadedScript>
      loadedScripts = Registries.newRegistry();

  public ScriptManager() {
    this.directory = PathUtil.getPluginDirectory("scripts");
    loaderFile = directory.resolve("loader.toml");

    //Set the language to ECMA Script 6 mode
    System.setProperty("nashorn.args.prepend", "--language=es6");

    // Save default scripts
    try {
      FtcJar.saveResources(
          "scripts",
          directory,
          ALLOW_OVERWRITE | OVERWRITE_IF_NEWER
      );
    } catch (IOException exc) {
      LOGGER.error("Couldn't save default scripts!", exc);
    }
  }

  @OnLoad
  public void load() {
    JsPreProcessor.placeHolders = null;

    loadedScripts.forEach(script -> {
      // Prevent script closing errors from
      // preventing script clear
      try {
        script.script().close();
      } catch (Throwable t) {
        LOGGER.error(
            "Unable to close script {}",
            script.script().getName(), t
        );
      }
    });
    loadedScripts.clear();

    if (!Files.exists(loaderFile)) {
      return;
    }

    SerializationHelper.readTomlAsJson(loaderFile, wrapper -> {
      for (var e : wrapper.entrySet()) {
        if (!Keys.isValidKey(e.getKey())) {
          LOGGER.warn("'{}' is an invalid key", e.getKey());
          continue;
        }

        var element = e.getValue();
        LoadedScript loadedScript;

        // Constantly loaded script
        if (element.isJsonPrimitive()) {
          Script script = Script.of(element.getAsString());
          loadedScript = new LoadedScript(null, script, null);
        }
        // Script loaded during a specific period
        else {
          JsonWrapper obj = JsonWrapper.wrap(element.getAsJsonObject());
          MonthDayPeriod period = null;
          String[] args = ArrayUtils.EMPTY_STRING_ARRAY;

          if (obj.has("period")) {
            period = MonthDayPeriod.load(obj.get("period"));
          }

          if (obj.has("args")) {
            args = obj.getArray(
                "args", JsonElement::getAsString, String[]::new
            );
          }

          Script script = Script.read(obj.get("script"), false);
          loadedScript = new LoadedScript(period, script, args);
        }

        LOGGER.debug("Loaded active script {}", e.getKey());
        loadedScripts.register(e.getKey(), loadedScript);
      }
    });

    // Load all scripts that should be active right now
    LocalDate date = LocalDate.now();
    for (var s : loadedScripts) {
      if (!s.shouldBeActive(date)) {
        continue;
      }

      s.load();
    }
  }

  @OnDayChange
  void onDayChange(ZonedDateTime time) {
    var date = time.toLocalDate();

    for (var s : loadedScripts) {
      var script = s.script();

      if (s.shouldBeActive(date)) {
        if (script.isCompiled()) {
          // Invoke the day change callback, if it exists
          script.invokeIfExists("__onDayChange", time);
          continue;
        }

        LOGGER.debug("Loading script {}", script.getName());
        s.load();
      } else if (script.isCompiled()) {
        LOGGER.debug("Closing script {}", script.getName());
        script.close();
      }
    }
  }

  public boolean isExistingScript(String script) {
    if (!script.endsWith(".js")) {
      return false;
    }

    var path = getScriptFile(script);
    return Files.exists(path);
  }

  public List<String> findExistingScripts() {
    return PathUtil.findAllFiles(directory, true)
        .resultOrPartial(LOGGER::error)
        .orElseGet(ObjectLists::emptyList)
        .stream()
        .filter(s -> s.endsWith(".js"))
        .collect(Collectors.toList());
  }

  public NashornScriptEngine createEngine(String... args) {
    return (NashornScriptEngine) factory.getScriptEngine(
        Objects.requireNonNullElse(args, ArrayUtils.EMPTY_STRING_ARRAY),
        getClassLoader(),
        this::canAccessClass
    );
  }

  private boolean canAccessClass(String className) {
    return !ILLEGAL_CLASSES.contains(className);
  }

  private ClassLoader getClassLoader() {
    return FTC.getPlugin()
        .getClass()
        .getClassLoader();
  }

  public Path getScriptFile(String name) {
    return directory.resolve(ensureSuffix(name));
  }

  private String ensureSuffix(String name) {
    Validate.isTrue(name.endsWith(".js"),
        "Script name '%s' does not have .js suffix", name
    );
    return name;
  }
}
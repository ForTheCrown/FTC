package net.forthecrown.scripts.pack;

import com.mojang.datafixers.util.Unit;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.scripts.CachingScriptLoader;
import net.forthecrown.scripts.Script;
import net.forthecrown.scripts.ScriptLoadException;
import net.forthecrown.scripts.ScriptService;
import net.forthecrown.scripts.module.JsModule;
import net.forthecrown.scripts.module.ModuleManager;
import net.forthecrown.scripts.module.ScriptModule;
import net.forthecrown.scripts.module.ScriptableModule;
import net.forthecrown.scripts.pack.PackExport.Export;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.io.source.Sources;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;

@Getter @Setter
public class ScriptPack {

  private static final Logger LOGGER = Loggers.getLogger();

  private final PackMeta meta;
  private final ScriptService service;

  private final CachingScriptLoader loader;

  private Script main;

  private final List<String> exportedModuleNames = new ArrayList<>();

  public ScriptPack(PackMeta meta, ScriptService service) {
    this.meta = meta;
    this.service = service;
    this.loader = service.newLoader(meta.getDirectory());
  }

  public void close() {
    loader.close();

    for (String exportedModuleName : exportedModuleNames) {
      service.getModules().remove(exportedModuleName);
    }
  }

  public boolean isActivated() {
    return main != null && main.isCompiled();
  }

  public Result<Unit> activate() {
    if (isActivated()) {
      return Result.error("Already activated");
    }

    for (String requiredPlugin : meta.getRequiredPlugins()) {
      if (PluginUtil.isEnabled(requiredPlugin)) {
        continue;
      }

      return Result.error("Required plugin '%s' wasn't enabled", requiredPlugin);
    }

    Source source = Sources.fromPath(meta.getMainScript(), meta.getDirectory());
    Script main = loader.loadScript(source);

    try {
      main.compile();
    } catch (ScriptLoadException exc) {
      return Result.error("Failed to load script '%s': '%s'",
          meta.getMainScript(), exc.getMessage()
      );
    }

    var result = main.evaluate()
        .toRegularResult()
        .map(o -> Unit.INSTANCE);

    if (result.isError()) {
      main.close();
    } else {
      this.main = main;
    }

    return result;
  }

  void createExports() {
    ModuleManager manager = service.getModules();
    var exports = meta.getExports();

    if (exports.isEmpty()) {
      LOGGER.debug("No exports created");
    }

    for (PackExport export : exports) {
      var file = export.getScriptFile();
      Result<Script> result = loader.loadCompiled(Sources.fromPath(file, meta.getDirectory()));

      if (result.isError()) {
        LOGGER.error("Error getting exports from script '{}': {}", file, result.getError());
        continue;
      }

      Script script = result.getValue();
      JsModule module;

      if (export.getExports().isEmpty()) {
        module = new ScriptModule(script);
      } else {
        NativeObject obj = new NativeObject();

        for (Export bindingExport : export.getExports()) {
          Object value = script.get(bindingExport.name());

          if (value == Scriptable.NOT_FOUND) {
            LOGGER.error("Couldn't find binding {} in {}", bindingExport.name(), script);
            continue;
          }

          String label = bindingExport.hasAlias()
              ? bindingExport.alias()
              : bindingExport.name();

          ScriptableObject.putProperty(obj, label, value);
        }

        module = new ScriptableModule(obj);
      }

      manager.addModule(export.getName(), module).apply(string -> {
        LOGGER.error("Couldn't register module '{}': {}", export.getName(), string);
      }, unit -> {
        exportedModuleNames.add(export.getName());
      });
    }
  }
}

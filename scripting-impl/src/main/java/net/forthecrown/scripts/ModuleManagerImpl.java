package net.forthecrown.scripts;

import com.google.common.base.Strings;
import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import net.forthecrown.Loggers;
import net.forthecrown.scripts.module.ImportInfo;
import net.forthecrown.scripts.module.ImportInfo.BindingImport;
import net.forthecrown.scripts.module.JsModule;
import net.forthecrown.scripts.module.ModuleManager;
import net.forthecrown.scripts.module.ScriptModule;
import net.forthecrown.scripts.module.ScriptableModule;
import net.forthecrown.scripts.modules.ScoreboardModule;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.io.source.Sources;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;

class ModuleManagerImpl implements ModuleManager {

  private static final Logger LOGGER = Loggers.getLogger();

  private final Map<String, JsModule> moduleMap = new Object2ObjectOpenHashMap<>();
  private final ScriptManager service;

  public ModuleManagerImpl(ScriptManager service) {
    this.service = service;
  }

  public void addBuiltIns() {
    addModule("scoreboard", ScoreboardModule.MODULE);
  }

  @Override
  public Result<Unit> addModule(String name, JsModule module) {
    if (Strings.isNullOrEmpty(name)) {
      return Result.error("Null name");
    }
    if (module == null) {
      return Result.error("Null module");
    }
    if (moduleMap.containsKey(name)) {
      return Result.error("Module already registered");
    }

    moduleMap.put(name, module);
    return Result.success(Unit.INSTANCE);
  }

  @Override
  public Optional<JsModule> getModule(String name) {
    return Optional.ofNullable(moduleMap.get(name));
  }

  @Override
  public void remove(String moduleName) {
    moduleMap.remove(moduleName);
  }

  @Override
  public Result<Unit> importInto(Script script, ImportInfo info) {
    Result<JsModule> searchResult = findModule(script, info.path());

    if (searchResult.isError()) {
      return searchResult.cast();
    }

    JsModule module = searchResult.getValue();
    Result<Unit> importResult = importModule(module, info, script.getScriptObject())
        .mapError(string -> {
          String prefix = "Failed to import values:";

          if (string.contains("\n")) {
            return prefix + "\n" + string;
          } else {
            return prefix + " " + string;
          }
        });

    if (importResult.isError()) {
      module.onImportFail(script);
    }

    return importResult;
  }

  Result<JsModule> findModule(Script script, String path) {
    JsModule module = moduleMap.get(path);

    if (module != null) {
      return Result.success(module);
    }

    Result<JsModule> fileModule = findScriptModule(script, path);
    if (fileModule != null) {
      return fileModule.mapError(s -> "Failed to load imported script: " + path + ": " + s);
    }

    Result<JsModule> classImport = tryImportClass(script, path);
    if (classImport != null) {
      return classImport.mapError(err -> "Failed to import '" + path + "' as class: " + err);
    }

    return Result.error("Couldn't find any class/script-file/module matching the name '%s'", path);
  }

  Result<JsModule> tryImportClass(Script script, String path) {
    Class result = tryLookupClass(path);

    if (result == null) {
      result = tryLookupClass(formatClassName(path));
    }

    if (result == null) {
      return null;
    }

    if (!Modifier.isPublic(result.getModifiers())) {
      return Result.error("Class %s is not a public class", result.getName());
    }

    return Result.success(createClassModule(script.getScriptObject(), result));
  }

  Class<?> tryLookupClass(String fqcn) {
    try {
      return Class.forName(fqcn, true, getClass().getClassLoader());
    } catch (ClassNotFoundException exc) {
      return null;
    }
  }

  String formatClassName(String fqcn) {
    String[] split = fqcn.split("\\.");
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < split.length; i++) {
      String s = split[i];

      if (i > 0) {
        String previous = split[i - 1];
        char first = previous.charAt(0);
        char connector;

        if (Character.isUpperCase(first)) {
          connector = '$';
        } else {
          connector = '.';
        }

        builder.append(connector);
      }

      builder.append(s);
    }

    return builder.toString();
  }

  ScriptableModule createClassModule(Scriptable scope, Class<?> clazz) {
    NativeJavaClass njc = new NativeJavaClass(scope, clazz);
    return new ScriptableModule(njc);
  }

  Result<JsModule> findScriptModule(Script script, String path) {
    String fName = path + (path.endsWith(".js") ? "" : ".js");

    Path dir = script.getWorkingDirectory();
    Path relative = script.getWorkingDirectory().resolve(fName);

    if (Files.exists(relative)) {
      Source source = Sources.fromPath(relative, dir);
      return loadModule(source, script);
    }

    Path scriptFile = service.getScriptsDirectory().resolve(fName);
    if (Files.exists(scriptFile)) {
      Source source = Sources.fromPath(scriptFile, dir);
      return loadModule(source, script);
    }

    return null;
  }

  Result<JsModule> loadModule(Source source, Script script) {
    ScriptLoader loader = script.getLoader();
    Script importedScript = loader.loadScript(source);

    try {
      importedScript.compile();
    } catch (Throwable t) {
      importedScript.getLogger().error("Failed to compile", t);
      return Result.error("Script loading failed");
    }

    var evalResult = importedScript.evaluate();
    if (evalResult.isSuccess()) {
      script.addChild(importedScript);
      return Result.success(new ScriptModule(importedScript));
    }

    evalResult.logError();
    return Result.error(evalResult.error().orElse("Script evaluation failed"));
  }

  Result<Unit> importModule(JsModule module, ImportInfo info, Scriptable scope) {
    var values = info.importedValues();
    var label = info.label();
    var aliased = info.isAlias();

    boolean starImport = values.size() == 1 && values.get(0).equals(BindingImport.STAR);

    Scriptable self = module.getSelfObject(scope);

    if (values.isEmpty() || (starImport && aliased)) {
      ScriptableObject.putConstProperty(scope, label, self);
      return Result.success(Unit.INSTANCE);
    }

    if (starImport) {
      if (self instanceof NativeObject no) {
        for (Entry<Object, Object> entry : no.entrySet()) {
          String key = entry.getKey().toString();
          ScriptableObject.putConstProperty(scope, key, entry.getValue());
        }
      } else {
        Object[] ids = self.getIds();
        for (Object id : ids) {
          var value = ScriptableModule.getProperty(self, id);
          ScriptableObject.putConstProperty(scope, id.toString(), value);
        }
      }

      return Result.success(Unit.INSTANCE);
    }

    Scriptable dest;

    if (aliased) {
      dest = new NativeObject();
    } else {
      dest = scope;
    }

    Set<String> alreadyImported = new HashSet<>();
    Result<Unit> result = Result.success(Unit.INSTANCE);

    for (BindingImport value : values) {
      Result<Object> bindingValue = getProperty(self, value.name())
          .mapError(s -> "Couldn't import binding '" + value.name() + "': " + s);

      if (bindingValue.isError() && !alreadyImported.isEmpty()) {
        alreadyImported.forEach(dest::delete);
        alreadyImported.clear();
      }

      result = result.combine(bindingValue, (err1, err2) -> err1 + "\n" + err2, (o, unit) -> {
        ScriptableObject.putConstProperty(dest, value.alias(), o);
        alreadyImported.add(value.alias());
        return unit;
      });
    }

    if (result.isError()) {
      return result;
    }

    if (aliased) {
      ScriptableObject.putConstProperty(scope, label, dest);
    }

    return Result.success(Unit.INSTANCE);
  }

  Result<Object> getProperty(Scriptable scriptable, String name) {
    var value = ScriptableObject.getProperty(scriptable, name);

    if (value == Scriptable.NOT_FOUND) {
      return Result.error("Not found");
    }

    return Result.success(value);
  }

  public void close() {
    moduleMap.forEach((string, jsModule) -> {
      jsModule.close();
    });
    moduleMap.clear();
  }
}

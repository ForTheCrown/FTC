package net.forthecrown.scripts;

import com.google.common.base.Strings;
import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import net.forthecrown.scripts.module.ImportInfo;
import net.forthecrown.scripts.module.ImportInfo.BindingImport;
import net.forthecrown.scripts.module.JsModule;
import net.forthecrown.scripts.module.ModuleManager;
import net.forthecrown.scripts.module.ScriptModule;
import net.forthecrown.scripts.module.ScriptableModule;
import net.forthecrown.scripts.modules.ParticlesModule;
import net.forthecrown.scripts.modules.ScoreboardModule;
import net.forthecrown.scripts.modules.WorldsObject;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.source.PathSource;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.io.source.Sources;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.TopLevel;

class ModuleManagerImpl implements ModuleManager {

  private final Map<String, JsModule> moduleMap = new Object2ObjectOpenHashMap<>();
  private final Set<String> autoImported = new ObjectOpenHashSet<>();

  private final ScriptManager service;

  public ModuleManagerImpl(ScriptManager service) {
    this.service = service;
  }

  public void addBuiltIns() {
    addModule("scoreboard", ScoreboardModule.MODULE);
    addModule("worlds",     WorldsObject.MODULE);
    addModule("particles",  ParticlesModule.MODULE);
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
  public Result<Unit> setAutoModule(String name, boolean state) {
    if (Strings.isNullOrEmpty(name)) {
      return Result.error("Null name");
    }
    if (!moduleMap.containsKey(name)) {
      return Result.error("No module with specified name");
    }

    if (state) {
      if (!autoImported.add(name)) {
        return Result.error("Module already set as auto import");
      }

      return Result.success(Unit.INSTANCE);
    }

    if (!autoImported.remove(name)) {
      return Result.error("Module is already non-auto imported");
    }

    return Result.success(Unit.INSTANCE);
  }

  @Override
  public Optional<JsModule> getModule(String name) {
    if (Strings.isNullOrEmpty(name)) {
      return Optional.empty();
    }
    return Optional.ofNullable(moduleMap.get(name));
  }

  @Override
  public void remove(String moduleName) {
    moduleMap.remove(moduleName);
    autoImported.remove(moduleName);
  }

  public Result<Unit> applyAutoImports(Script script) {
    Result<Unit> result = Result.success(Unit.INSTANCE);
    Set<String> alreadyImported = new HashSet<>();

    for (String s : autoImported) {
      JsModule module = moduleMap.get(s);

      if (module == null) {
        continue;
      }

      Result<Unit> importResult = importModule(
          module,
          new ImportInfo(true, s, s, List.of()),
          script.getScriptObject()
      );

      if (importResult.isError()) {
        if (!alreadyImported.isEmpty()) {
          alreadyImported.forEach(script::remove);
          alreadyImported.clear();
        }
      }

      result = result.combine(importResult, (s1, s2) -> s1 + "; " + s2, (unit, unit2) -> {
        alreadyImported.add(s);
        return unit;
      });
    }

    return result;
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

    if (script.getSource() instanceof PathSource source) {
      Path file = source.path().getParent();

      if (file != null) {
        relative = file.resolve(fName);

        if (Files.exists(relative)) {
          Source s1 = Sources.fromPath(relative, source.directory());
          return loadModule(s1, script);
        }
      }
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

    if (importedScript.isCompiled()) {
      return Result.success(new ScriptModule(importedScript));
    }

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
      var destObject = new NativeObject();
      ScriptRuntime.setBuiltinProtoAndParent(destObject, scope, TopLevel.Builtins.Object);
      dest = destObject;
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

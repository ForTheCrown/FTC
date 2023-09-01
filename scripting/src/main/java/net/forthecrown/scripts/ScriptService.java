package net.forthecrown.scripts;

import java.nio.file.Path;
import java.util.List;
import net.forthecrown.scripts.module.ModuleManager;
import net.forthecrown.utils.io.source.Source;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface ScriptService {

  Path getScriptsDirectory();

  @NotNull
  Script newScript(@NotNull Source source);

  Plugin getScriptPlugin();

  List<Class<?>> getAutoImportedClasses();

  void addActiveScript(Script script);

  ModuleManager getModules();
}
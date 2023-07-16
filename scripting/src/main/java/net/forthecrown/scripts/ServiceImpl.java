package net.forthecrown.scripts;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.forthecrown.Worlds;
import net.forthecrown.text.Text;
import net.forthecrown.utils.io.source.Source;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

class ServiceImpl implements ScriptService {

  @Getter
  private final Path scriptsDirectory;

  @Getter
  private final Plugin scriptPlugin;

  @Getter
  private final List<Class<?>> autoImportedClasses = new ObjectArrayList<>();

  private final List<RhinoScript> active = new ArrayList<>();

  public ServiceImpl(Path scriptsDirectory, Plugin scriptPlugin) {
    this.scriptsDirectory = scriptsDirectory;
    this.scriptPlugin = scriptPlugin;

    setupAutoImports();
  }

  private void setupAutoImports() {
    autoImportedClasses.add(Text.class);
    autoImportedClasses.add(Component.class);
    autoImportedClasses.add(Material.class);
    autoImportedClasses.add(EntityType.class);
    autoImportedClasses.add(Worlds.class);
    autoImportedClasses.add(Bukkit.class);
  }

  @Override
  public @NotNull Script newScript(@NotNull Source source) {
    Script script = new RhinoScript(source);

    script.setWorkingDirectory(scriptsDirectory);
    autoImportedClasses.forEach(script::importClass);

    script.addExtension("events", new EventsExtension(scriptPlugin));
    script.addExtension("scheduler", new SchedulerExtension());

    return script;
  }

  @Override
  public void addActiveScript(Script script) {
    active.add((RhinoScript) script);
  }

  public void close() {
    active.forEach(RhinoScript::close);
    active.clear();


  }
}
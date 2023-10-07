package net.forthecrown.scripts;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import net.forthecrown.Worlds;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

public class ScriptManager implements ScriptService {

  @Getter
  private final Path scriptsDirectory;

  @Getter
  private final Plugin scriptPlugin;

  private final List<Class<?>> autoImportedClasses = new ObjectArrayList<>();

  @Getter
  private final CachingScriptLoader globalLoader;

  @Getter
  private final ModuleManagerImpl modules;

  private NativeObject topLevelScope;

  public ScriptManager(Path scriptsDirectory, Plugin scriptPlugin) {
    this.scriptsDirectory = scriptsDirectory;
    this.scriptPlugin = scriptPlugin;

    this.modules = new ModuleManagerImpl(this);
    this.modules.addBuiltIns();

    this.globalLoader = newLoader();

    setupAutoImports();
  }

  private void setupAutoImports() {
    autoImportedClasses.add(Vector3i.class);
    autoImportedClasses.add(Vector3d.class);
    autoImportedClasses.add(Vector2i.class);
    autoImportedClasses.add(Vector2d.class);
    autoImportedClasses.add(WorldBounds3i.class);
    autoImportedClasses.add(Bounds3i.class);
    autoImportedClasses.add(WorldVec3i.class);
    autoImportedClasses.add(Cooldown.class);
    autoImportedClasses.add(Bukkit.class);
    autoImportedClasses.add(Material.class);
    autoImportedClasses.add(EntityType.class);
    autoImportedClasses.add(Vectors.class);
    autoImportedClasses.add(Location.class);
    autoImportedClasses.add(Component.class);
    autoImportedClasses.add(Text.class);
    autoImportedClasses.add(NamedTextColor.class);
    autoImportedClasses.add(Messages.class);
    autoImportedClasses.add(Style.class);
    autoImportedClasses.add(Users.class);
    autoImportedClasses.add(HoverEvent.class);
    autoImportedClasses.add(TextDecoration.class);
    autoImportedClasses.add(ClickEvent.class);
    autoImportedClasses.add(TextColor.class);
    autoImportedClasses.add(ItemStacks.class);
    autoImportedClasses.add(Worlds.class);
  }

  public NativeObject getTopLevelScope(Context ctx) {
    if (topLevelScope != null) {
      return topLevelScope;
    }

    topLevelScope = new NativeObject();
    ctx.initStandardObjects(topLevelScope);

    for (Class<?> autoImportedClass : autoImportedClasses) {
      NativeJavaClass njc = new NativeJavaClass(topLevelScope, autoImportedClass);
      ScriptableObject.putConstProperty(topLevelScope, autoImportedClass.getSimpleName(), njc);
    }

    return topLevelScope;
  }

  @Override
  public CachingScriptLoader newLoader() {
    return new CachingLoaderImpl(this);
  }

  @Override
  public CachingScriptLoader newLoader(Path workingDirectory) {
    return new CachingLoaderImpl(this, workingDirectory);
  }

  @Override
  public void addAutoImportedClass(Class<?> clazz) {
    Objects.requireNonNull(clazz, "Class was null");

    if (autoImportedClasses.contains(clazz)) {
      return;
    }

    autoImportedClasses.add(clazz);

    if (topLevelScope == null) {
      return;
    }

    NativeJavaClass njc = new NativeJavaClass(topLevelScope, clazz);
    ScriptableObject.putConstProperty(topLevelScope, clazz.getSimpleName(), njc);
  }

  public List<Class<?>> getAutoImportedClasses() {
    return Collections.unmodifiableList(autoImportedClasses);
  }

  @Override
  public Script loadScript(Source source) {
    return newScript(source);
  }

  @Override
  public @NotNull Script newScript(ScriptLoader loader, @NotNull Source source) {
    Script script = new RhinoScript(loader, this, source);

    script.setWorkingDirectory(scriptsDirectory);

    script.addExtension("events", new EventsExtension(scriptPlugin));
    script.addExtension("scheduler", new SchedulerExtension());

    return script;
  }

  public void close() {
    modules.close();
    globalLoader.close();
  }
}


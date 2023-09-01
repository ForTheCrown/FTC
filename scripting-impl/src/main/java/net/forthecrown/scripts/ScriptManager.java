package net.forthecrown.scripts;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

public class ScriptManager implements ScriptService {

  @Getter
  private final Path scriptsDirectory;

  @Getter
  private final Plugin scriptPlugin;

  @Getter
  private final List<Class<?>> autoImportedClasses = new ObjectArrayList<>();

  private final List<RhinoScript> active = new ArrayList<>();

  @Getter
  private final ModuleManagerImpl modules;

  public ScriptManager(Path scriptsDirectory, Plugin scriptPlugin) {
    this.scriptsDirectory = scriptsDirectory;
    this.scriptPlugin = scriptPlugin;

    this.modules = new ModuleManagerImpl(this);
    this.modules.addBuiltIns();

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

  @Override
  public @NotNull Script newScript(@NotNull Source source) {
    Script script = new RhinoScript(this, source);

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


package net.forthecrown.core.script2;

import java.nio.file.Files;
import java.util.function.Function;
import jdk.dynalink.beans.StaticClass;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

public final class ScriptsBuiltIn {
  private ScriptsBuiltIn() {}

  private static final Class[] DEFAULT_CLASSES = {
      Util.class,           FTC.class,
      Vector3i.class,       Vector3d.class,
      Vector2i.class,       Vector2d.class,
      WorldBounds3i.class,  Bounds3i.class,
      WorldVec3i.class,     Cooldown.class,
      Bukkit.class,         Material.class,
      EntityType.class,     Vectors.class,
      Location.class,       Component.class,
      Text.class,           NamedTextColor.class,
      Messages.class,       Style.class,
      Users.class,          HoverEvent.class,
      TextDecoration.class, ClickEvent.class,
      TextColor.class,      ItemStacks.class,
      Worlds.class
  };

  public static Function<String, WrappedScript> compileFunction(Script script) {
    return scriptName -> {
      var file = script.getWorkingDirectory().resolve(scriptName);

      if (!Files.exists(file)) {
        throw new IllegalStateException(
            "File " + scriptName + " doesn't exist"
        );
      }

      Script loaded = Script.of(file).compile();
      var wrapped = new WrappedScript(loaded);
      script.getLoadedSubScripts().add(wrapped);

      return wrapped;
    };
  }

  public static void populate(String name, NashornScriptEngine engine) {
    for (var c : DEFAULT_CLASSES) {
      engine.put(c.getSimpleName(), StaticClass.forClass(c));
    }

    Logger logger = Loggers.getLogger(name);
    engine.put("logger", logger);
  }
}
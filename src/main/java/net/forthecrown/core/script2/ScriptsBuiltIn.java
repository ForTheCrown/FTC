package net.forthecrown.core.script2;

import com.google.common.base.Preconditions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import jdk.dynalink.beans.StaticClass;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Worlds;
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
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
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

  static final JsCallback COMPILE_FUNCTION = (script, invoker, args) -> {
    Preconditions.checkArgument(args.length > 0, "Script name required");

    Path file = script.getWorkingDirectory()
        .resolve(String.valueOf(args[0]));

    Preconditions.checkArgument(
        Files.exists(file),
        "Script '%s' doesn't exist", file
    );

    String[] scriptArgs;
    if (args.length > 1) {
      scriptArgs = Arrays.stream(args, 1, args.length)
          .map(String::valueOf)
          .toArray(String[]::new);
    } else {
      scriptArgs = ArrayUtils.EMPTY_STRING_ARRAY;
    }

    Script loaded = Script.of(file).compile(scriptArgs);
    var wrapped = new WrappedScript(loaded);
    script.getLoadedSubScripts().add(wrapped);

    return wrapped;
  };

  public static void populate(Script script) {
    for (var c : DEFAULT_CLASSES) {
      script.put(c.getSimpleName(), StaticClass.forClass(c));
    }
  }
}
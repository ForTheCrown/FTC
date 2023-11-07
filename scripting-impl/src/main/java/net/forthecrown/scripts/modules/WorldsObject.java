package net.forthecrown.scripts.modules;

import com.google.common.base.Strings;
import java.util.Optional;
import net.forthecrown.Worlds;
import net.forthecrown.scripts.module.JsModule;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Symbol;

public class WorldsObject extends ScriptableObject {

  public static final JsModule MODULE = scope -> {
    WorldsObject object = new WorldsObject();
    object.setParentScope(scope);
    return object;
  };

  @Override
  public boolean has(String name, Scriptable start) {
    return getWorld(name).isPresent();
  }

  @Override
  public void put(String name, Scriptable start, Object value) {
    // No-op
  }

  @Override
  public boolean has(int index, Scriptable start) {
    return false;
  }

  @Override
  public boolean has(Symbol key, Scriptable start) {
    return false;
  }

  @Override
  public Object get(String name, Scriptable start) {
    return getWorld(name)
        .map(world -> (Object) new NativeJavaObject(start, world, World.class))
        .orElse(NOT_FOUND);
  }

  @Override
  public Object get(int index, Scriptable start) {
    return NOT_FOUND;
  }

  @Override
  public Object get(Symbol key, Scriptable start) {
    return NOT_FOUND;
  }

  private Optional<World> getWorld(String v) {
    if (Strings.isNullOrEmpty(v)) {
      return Optional.empty();
    }

    if (v.equalsIgnoreCase("overworld")) {
      return Optional.of(Worlds.overworld());
    }

    if (v.equalsIgnoreCase("nether")) {
      return Optional.of(Worlds.nether());
    }

    if (v.equalsIgnoreCase("end") || v.equalsIgnoreCase("the_end")) {
      return Optional.of(Worlds.end());
    }

    if (v.contains(":")) {
      NamespacedKey key = NamespacedKey.fromString(v);
      if (key == null) {
        throw ScriptRuntime.typeError("Invalid namespaced key: '" + v + "'");
      }
      return Optional.ofNullable(Bukkit.getWorld(key));
    }

    return Optional.ofNullable(Bukkit.getWorld(v));
  }

  @Override
  public String getClassName() {
    return "Worlds";
  }
}

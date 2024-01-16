package net.forthecrown.scripts;

import static org.mozilla.javascript.ScriptableObject.getProperty;
import static org.mozilla.javascript.ScriptableObject.hasProperty;

import com.google.gson.JsonParser;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;
import net.forthecrown.Worlds;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.UserLookup.LookupEntry;
import net.forthecrown.user.Users;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

public final class ScriptUtils {
  private ScriptUtils() {}

  public static void ensureParameterCount(Object[] args, int requiredLength) {
    if (args.length >= requiredLength) {
      return;
    }

    throw ScriptRuntime.typeError(
        "Expected " + requiredLength + " arguments, found " + args.length
    );
  }

  public static RuntimeException cantLoad(String typename, Object v) {
    return ScriptRuntime.typeError("Don't know how to load " + typename + " from: " + v);
  }

  public static Vector2d toVec2d(Object[] args, int index) {
    return args.length <= index ? null : toVec2d(args[index]);
  }

  public static Vector2d toVec2d(Object value) {
    value = Context.jsToJava(value, Object.class);

    if (value instanceof Vector2d vec2) {
      return vec2;
    }
    if (value instanceof org.spongepowered.math.vector.Vector2d vec2) {
      return new Vector2d(vec2.x(), vec2.y());
    }
    if (value instanceof Scriptable scriptable) {
      double x;
      double y;

      x = ScriptRuntime.toNumber(getProperty(scriptable, "x"));

      if (hasProperty(scriptable, "y")) {
        y = ScriptRuntime.toNumber(getProperty(scriptable, "y"));
      } else if (hasProperty(scriptable, "z")) {
        y = ScriptRuntime.toNumber(getProperty(scriptable, "z"));
      } else {
        throw ScriptRuntime.typeError("Missing 'y' component of vector object");
      }

      return new Vector2d(x, y);
    }

    throw cantLoad("Vector2d", value);
  }

  public static Vector3d toVec3d(Object[] args, int index) {
    return args.length <= index ? null : toVec3d(args[index]);
  }

  public static Vector3d toVec3d(Object value) {
    value = Context.jsToJava(value, Object.class);

    if (value instanceof Vector3d vec3) {
      return vec3;
    }
    if (value instanceof org.spongepowered.math.vector.Vector3d sponge) {
      return new Vector3d(sponge.x(), sponge.y(), sponge.z());
    }
    if (value instanceof Scriptable scriptable) {
      double x = ScriptRuntime.toNumber(getProperty(scriptable, "x"));
      double y = ScriptRuntime.toNumber(getProperty(scriptable, "y"));
      double z = ScriptRuntime.toNumber(getProperty(scriptable, "z"));
      return new Vector3d(x, y, z);
    }

    throw cantLoad("Vector3d", value);
  }

  public static World toWorld(Object[] args, int index) {
    return args.length <= index ? null : toWorld(args[index]);
  }

  public static World toWorld(Object value) {
    value = Context.jsToJava(value, Object.class);

    World world;

    if (value instanceof World w) {
      return w;
    } else if (value instanceof NamespacedKey namespacedKey) {
      world = Bukkit.getWorld(namespacedKey);
    } else if (value instanceof Key key) {
      world = Bukkit.getWorld(new NamespacedKey(key.namespace(), key.value()));
    } else if (value instanceof Entity entity) {
      return entity.getWorld();
    } else if (value instanceof Block block) {
      return block.getWorld();
    } else if (value instanceof BlockState state) {
      return state.getWorld();
    } else if (value instanceof CommandSource source) {
      return source.getWorld();
    } else if (value instanceof CharSequence sequence) {
      var v = sequence.toString();
      if (v.equalsIgnoreCase("overworld")) {
        return Worlds.overworld();
      }

      if (v.equalsIgnoreCase("nether")) {
        return Worlds.nether();
      }

      if (v.equalsIgnoreCase("end") || v.equalsIgnoreCase("the_end")) {
        return Worlds.end();
      }

      if (v.contains(":")) {
        NamespacedKey key = NamespacedKey.fromString(v);

        if (key == null) {
          throw ScriptRuntime.typeError("Invalid namespaced key: '" + v + "'");
        }

        world = Bukkit.getWorld(key);
      } else {
        world = Bukkit.getWorld(v);
      }
    } else {
      throw cantLoad("World", value);
    }

    if (world == null) {
      throw ScriptRuntime.typeError("Unknown world: '" + value + "'");
    }

    return world;
  }

  public static Component toText(Context context, Scriptable scope, Object[] args, int index) {
    if (args.length <= index) {
      return null;
    }

    Object val = args[index];
    return toText(context, scope, val);
  }

  public static Component toText(Context context, Scriptable scope, Object val) {
    if (val == null || Undefined.isUndefined(val)) {
      return null;
    }

    if (val instanceof Scriptable scriptable) {
      String json = String.valueOf(NativeJSON.stringify(context, scope, scriptable, null, null));
      var element = JsonParser.parseString(json);
      return GsonComponentSerializer.gson().deserializeFromTree(element);
    }

    return Text.valueOf(val);
  }

  public static ItemStack toItemStack(Object[] args, int index) {
    if (args.length <= index) {
      return null;
    }

    Object value = args[index];
    return toItemStack(value);
  }

  public static ItemStack toItemStack(Object value) {
    Object jType = Context.jsToJava(value, Object.class);

    if (jType instanceof CharSequence string) {
      return ItemStacks.fromNbtString(string.toString());
    }

    if (jType instanceof CompoundTag tag) {
      return ItemStacks.load(tag);
    }

    if (jType instanceof ItemStack itemStack) {
      return itemStack;
    }

    throw cantLoad("item stack", value);
  }

  public static BlockData toBlockData(Object o) {
    o = Context.jsToJava(o, Object.class);

    if (o instanceof CharSequence sequence) {
      StringReader reader = new StringReader(sequence.toString());
      try {
        return ArgumentTypes.block().parse(reader).getParsedState();
      } catch (CommandSyntaxException exc) {
        var message = exc.getMessage();
        throw ScriptRuntime.typeError(message);
      }
    }

    if (o instanceof Material material) {
      return material.createBlockData();
    }

    if (o instanceof BlockData data) {
      return data;
    }

    throw cantLoad("BlockData", o);
  }

  public static CommandSource toSource(Object[] args, int index) {
    if (args.length <= index) {
      return null;
    }

    Object value = args[index];
    if (value instanceof String string) {
      if (string.equalsIgnoreCase("console") || string.equalsIgnoreCase("server")) {
        return Grenadier.createSource(Bukkit.getConsoleSender());
      }

      UserLookup lookup = Users.getService().getLookup();
      LookupEntry entry = lookup.query(string);

      if (entry == null) {
        throw ScriptRuntime.typeError("Unknown player name: '" + string + "'");
      }

      var player = Users.get(entry);

      if (!player.isOnline()) {
        throw ScriptRuntime.typeError("Player not online: " + player.getName());
      }

      return player.getCommandSource();
    }

    Object o = Context.jsToJava(value, Object.class);

    if (o instanceof UUID uuid) {
      o = Bukkit.getEntity(uuid);
    }

    if (o instanceof User user) {
      if (!user.isOnline()) {
        throw ScriptRuntime.typeError("User " + user.getName() + " is not online");
      }

      return Grenadier.createSource(user.getPlayer());
    }

    if (o instanceof CommandSender sender) {
      return Grenadier.createSource(sender);
    }

    if (o instanceof CommandSource source) {
      return source;
    }

    return null;
  }

  public static String concatArgs(Object[] args, int startArg) {
    if (args == null || args.length <= startArg) {
      return "";
    }

    StringBuilder builder = new StringBuilder();

    for (int i = startArg; i < args.length; i++) {
      Object arg = args[i];
      builder.append(ScriptRuntime.toString(arg));
    }

    return builder.toString();
  }

  public static <E extends Enum<E>> E toEnum(Object[] args, int i, Class<E> sourceClass) {
    if (args.length <= i) {
      return null;
    }

    Object arg = Context.jsToJava(args[i], Object.class);

    if (arg instanceof CharSequence sequence) {
      String str = sequence.toString();
      E[] values = sourceClass.getEnumConstants();

      for (E value : values) {
        if (value.name().equalsIgnoreCase(str)) {
          return value;
        }
      }

      throw ScriptRuntime.typeError(
          "Unknown " + sourceClass.getSimpleName() + " value: '" + str + "'"
      );
    }

    if (sourceClass.isInstance(arg)) {
      return sourceClass.cast(arg);
    }

    throw cantLoad(sourceClass.getSimpleName(), arg);
  }
}

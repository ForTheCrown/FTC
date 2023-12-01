package net.forthecrown.scripts;

import com.google.gson.JsonParser;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

public final class ScriptUtils {
  private ScriptUtils() {}

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

    throw ScriptRuntime.typeError("Not an itemstack " + value);
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

      Player player = Bukkit.getPlayer(string);

      if (player == null) {
        return null;
      }

      return Grenadier.createSource(player);
    }

    Object o = Context.jsToJava(value, Object.class);

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
}

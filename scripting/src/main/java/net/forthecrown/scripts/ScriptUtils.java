package net.forthecrown.scripts;

import com.google.gson.JsonParser;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

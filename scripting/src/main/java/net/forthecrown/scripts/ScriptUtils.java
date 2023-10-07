package net.forthecrown.scripts;

import com.google.gson.JsonParser;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJSON;
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
}

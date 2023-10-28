package net.forthecrown.scripts.builtin;

import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.scripts.ScriptUtils;
import org.bukkit.Bukkit;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

public class FtcScriptRuntime {

  static final Callable EXEC_CONSOLE = (cx, scope, thisObj, args) -> {
    return runString(Grenadier.createSource(Bukkit.getConsoleSender()), args,0);
  };

  static final Callable EXEC_AS = (cx, scope, thisObj, args) -> {
    if (args.length == 0) {
      return null;
    }

    CommandSource source = ScriptUtils.toSource(args, 0);

    if (source == null) {
      source = Grenadier.createSource(Bukkit.getConsoleSender());
    }

    return runString(source, args, 1);
  };

  static final Callable GIVE_ITEM = new GiveItemFunction();

  public static void initStandardObjects(NativeObject object) {
    ScriptableObject.putConstProperty(object, "command", EXEC_CONSOLE);
    ScriptableObject.putConstProperty(object, "runAs", EXEC_AS);
    ScriptableObject.putConstProperty(object, "giveItem", GIVE_ITEM);
  }

  static Object runString(CommandSource sender, Object[] args, int argsStart) {
    String command = ScriptUtils.concatArgs(args, argsStart);

    try {
      return Grenadier.dispatch(sender, command);
    } catch (Throwable t) {
      t.printStackTrace();
      return null;
    }
  }
}

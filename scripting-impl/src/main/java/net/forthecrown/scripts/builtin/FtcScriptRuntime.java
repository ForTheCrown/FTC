package net.forthecrown.scripts.builtin;

import net.forthecrown.FtcServer;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.scripts.ScriptUtils;
import org.bukkit.Bukkit;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
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

  static final Callable RENDER_PLACEHOLDERS = new RenderPlaceholdersFunction();
  static final Callable SEND_MESSAGE = new SendMessageFunction(false);
  static final Callable SEND_ACTION_BAR = new SendMessageFunction(true);
  static final Callable PLAY_SOUND = new PlaySoundFunction();

  static final Callable TIME_MILLIS = (cx, scope, thisObj, args) -> System.currentTimeMillis();
  static final Callable TIME_SECONDS = (cx, scope, thisObj, args) -> {
    double timeMillis = System.currentTimeMillis();
    return timeMillis / 1000.0d;
  };

  public static void initStandardObjects(Scriptable object) {
    ScriptableObject.putConstProperty(object, "command", EXEC_CONSOLE);
    ScriptableObject.putConstProperty(object, "runAs", EXEC_AS);

    ScriptableObject.putConstProperty(object, "giveItem", GIVE_ITEM);

    ScriptableObject.putConstProperty(object, "currentTimeMillis", TIME_MILLIS);
    ScriptableObject.putConstProperty(object, "currentTimeSeconds", TIME_SECONDS);

    ScriptableObject.putConstProperty(object, "sendMessage", SEND_MESSAGE);
    ScriptableObject.putConstProperty(object, "sendActionBar", SEND_ACTION_BAR);
    ScriptableObject.putConstProperty(object, "renderPlaceholders", RENDER_PLACEHOLDERS);
    ScriptableObject.putConstProperty(object, "playSound", PLAY_SOUND);

    ScriptableObject.putConstProperty(
        object,
        "ftcServer",
        Context.javaToJS(FtcServer.server(), object)
    );

    ScriptableObject.putConstProperty(
        object,
        "server",
        Context.javaToJS(Bukkit.getServer(), object)
    );

    NativeVectors.init(object);
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

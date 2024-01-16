package net.forthecrown.scripts.builtin;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.forthecrown.scripts.Script;
import net.forthecrown.scripts.ScriptUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JsConsole extends IdScriptableObject {

  static final int ID_assert = 1;
  static final int ID_clear = 2;
  static final int ID_count = 3;
  static final int ID_countReset = 4;
  static final int ID_debug = 5;
  static final int ID_dir = 6;
  static final int ID_dirxml = 7;
  static final int ID_error = 8;
  static final int ID_exception = 9;
  static final int ID_group = 10;
  static final int ID_groupCollapsed = 11;
  static final int ID_groupEnd = 12;
  static final int ID_info = 13;
  static final int ID_log = 14;
  static final int ID_profile = 15;
  static final int ID_profileEnd = 16;
  static final int ID_table = 17;
  static final int ID_time = 18;
  static final int ID_timeEnd = 19;
  static final int ID_timeLog = 20;
  static final int ID_timeStamp = 21;
  static final int ID_trace = 22;
  static final int ID_warn = 23;

  static final int MAX_ID = ID_warn;

  static final String NAME_assert = "assert";
  static final String NAME_clear = "clear";
  static final String NAME_count = "count";
  static final String NAME_countReset = "countReset";
  static final String NAME_debug = "debug";
  static final String NAME_dir = "dir";
  static final String NAME_dirxml = "dirxml";
  static final String NAME_error = "error";
  static final String NAME_exception = "exception";
  static final String NAME_group = "group";
  static final String NAME_groupCollapsed = "groupCollapsed";
  static final String NAME_groupEnd = "groupEnd";
  static final String NAME_info = "info";
  static final String NAME_log = "log";
  static final String NAME_profile = "profile";
  static final String NAME_profileEnd = "profileEnd";
  static final String NAME_table = "table";
  static final String NAME_time = "time";
  static final String NAME_timeEnd = "timeEnd";
  static final String NAME_timeLog = "timeLog";
  static final String NAME_timeStamp = "timeStamp";
  static final String NAME_trace = "trace";
  static final String NAME_warn = "warn";

  private final Script script;
  private final Object2LongMap<String> timers;

  public JsConsole(Script script) {
    this.script = script;
    this.timers = new Object2LongOpenHashMap<>();
  }

  static void init(Scriptable scope, Script script) {
    JsConsole console = new JsConsole(script);
    console.setParentScope(scope);
    console.activatePrototypeMap(MAX_ID);

    ScriptableObject.putConstProperty(scope, "console", console);
  }

  @Override
  public String getClassName() {
    return "console";
  }

  @Override
  protected int getMaxInstanceId() {
    return MAX_ID;
  }

  @Override
  protected Object getInstanceIdValue(int id) {
    return NOT_FOUND;
  }

  @Override
  public Object[] getIds() {
    Object[] ids = new Object[MAX_ID];
    for (int i = 1; i <= MAX_ID; i++) {
      ids[i-1] = getInstanceIdName(i);
    }
    return ids;
  }

  @Override
  protected void initPrototypeId(int id) {
    String name = getInstanceIdName(id);
    int arity = switch (id) {
      case ID_assert -> 1;
      case ID_clear -> 1;
      case ID_count -> 1;
      case ID_countReset -> 1;
      case ID_debug -> 1;
      case ID_dir -> 1;
      case ID_dirxml -> 1;
      case ID_error -> 1;
      case ID_exception -> 1;
      case ID_group -> 1;
      case ID_groupCollapsed -> 1;
      case ID_groupEnd -> 1;
      case ID_info -> 1;
      case ID_log -> 1;
      case ID_profile -> 1;
      case ID_profileEnd -> 1;
      case ID_table -> 1;
      case ID_time -> 1;
      case ID_timeEnd -> 1;
      case ID_timeLog -> 1;
      case ID_timeStamp -> 1;
      case ID_trace -> 1;
      case ID_warn -> 1;
      default -> 1;
    };

    initPrototypeMethod("console", id, name, arity);
  }

  @Override
  protected String getInstanceIdName(int id) {
    return switch (id) {
      case ID_assert -> NAME_assert;
      case ID_clear -> NAME_clear;
      case ID_count -> NAME_count;
      case ID_countReset -> NAME_countReset;
      case ID_debug -> NAME_debug;
      case ID_dir -> NAME_dir;
      case ID_dirxml -> NAME_dirxml;
      case ID_error -> NAME_error;
      case ID_exception -> NAME_exception;
      case ID_group -> NAME_group;
      case ID_groupCollapsed -> NAME_groupCollapsed;
      case ID_groupEnd -> NAME_groupEnd;
      case ID_info -> NAME_info;
      case ID_log -> NAME_log;
      case ID_profile -> NAME_profile;
      case ID_profileEnd -> NAME_profileEnd;
      case ID_table -> NAME_table;
      case ID_time -> NAME_time;
      case ID_timeEnd -> NAME_timeEnd;
      case ID_timeLog -> NAME_timeLog;
      case ID_timeStamp -> NAME_timeStamp;
      case ID_trace -> NAME_trace;
      case ID_warn -> NAME_warn;

      default -> throw new IllegalArgumentException(String.valueOf(id));
    };
  }

  @Override
  protected int findInstanceIdInfo(String name) {
    return findPrototypeId(name);
  }

  @Override
  protected int findPrototypeId(String name) {
    return switch (name) {
      case NAME_assert -> ID_assert;
      case NAME_clear -> ID_clear;
      case NAME_count -> ID_count;
      case NAME_countReset -> ID_countReset;
      case NAME_debug -> ID_debug;
      case NAME_dir -> ID_dir;
      case NAME_dirxml -> ID_dirxml;
      case NAME_error -> ID_error;
      case NAME_exception -> ID_exception;
      case NAME_group -> ID_group;
      case NAME_groupCollapsed -> ID_groupCollapsed;
      case NAME_groupEnd -> ID_groupEnd;
      case NAME_info -> ID_info;
      case NAME_log -> ID_log;
      case NAME_profile -> ID_profile;
      case NAME_profileEnd -> ID_profileEnd;
      case NAME_table -> ID_table;
      case NAME_time -> ID_time;
      case NAME_timeEnd -> ID_timeEnd;
      case NAME_timeLog -> ID_timeLog;
      case NAME_timeStamp -> ID_timeStamp;
      case NAME_trace -> ID_trace;
      case NAME_warn -> ID_warn;
      default -> 0;
    };
  }

  @Override
  public Object execIdCall(
      IdFunctionObject f,
      Context cx,
      Scriptable scope,
      Scriptable thisObj,
      Object[] args
  ) {
    switch (f.methodId()) {
      case ID_assert -> {

      }

      case ID_clear -> {

      }

      case ID_count -> {

      }

      case ID_countReset -> {

      }

      case ID_debug -> {

      }

      case ID_dir -> {

      }

      case ID_dirxml -> {

      }

      case ID_error -> {
        String combinedArgs = concatArgs(args);
        script.getLogger().error(combinedArgs);
      }

      case ID_group -> {

      }

      case ID_groupCollapsed -> {

      }

      case ID_groupEnd -> {

      }

      case ID_info -> {

      }

      case ID_log -> {
        String combinedArgs = concatArgs(args);
        script.getLogger().info(combinedArgs);
      }

      case ID_profile -> {

      }

      case ID_profileEnd -> {

      }

      case ID_table -> {

      }

      case ID_time -> {

      }

      case ID_timeEnd -> {

      }

      case ID_timeLog -> {

      }

      case ID_timeStamp -> throw unsupported(NAME_timeStamp);

      case ID_trace -> {
        String message = concatArgs(args);
        Throwable throwable = ScriptRuntime.typeError("trace");

        script.getLogger().info(
            Strings.isNullOrEmpty(message) ? "console.trace()" : message,
            throwable
        );
      }

      case ID_warn -> {
        String combinedArgs = concatArgs(args);
        script.getLogger().warn(combinedArgs);
      }

      default -> throw f.unknown();
    }

    return Context.getUndefinedValue();
  }

  RuntimeException unsupported(String label) {
    return new UnsupportedOperationException("console." + label + "() is not supported");
  }

  static String concatArgs(Object[] args) {
    if (args.length == 0) {
      return "";
    }

    Object first = args[0];
    Object jType = Context.jsToJava(first, Object.class);

    if (jType instanceof CharSequence sequence) {
      String firstStr = sequence.toString();
      if (args.length == 1) {
        return firstStr;
      }

      Object[] subArray = new Object[args.length - 1];
      for (int i = 1; i < args.length; i++) {
        Object arg = args[i];
        subArray[i] = Context.jsToJava(arg, Object.class);
      }

      return String.format(firstStr, subArray);
    }

    return ScriptUtils.concatArgs(args, 0);
  }
}

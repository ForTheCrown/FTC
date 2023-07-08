package net.forthecrown.scripts;

import static net.forthecrown.scripts.RhinoScript.EMPTY_OBJECT_ARRAY;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

class CallbackWrapper extends BaseFunction {

  private final ScriptCallback callback;
  private final Script script;

  public CallbackWrapper(ScriptCallback callback, Script script) {
    this.callback = callback;
    this.script = script;
  }

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
    Object[] jArgs;

    if (args == null || args.length < 1) {
      jArgs = EMPTY_OBJECT_ARRAY;
    } else {
      jArgs = new Object[args.length];

      for (int i = 0; i < args.length; i++) {
        jArgs[i] = RhinoScript.toJava(args[i]);
      }
    }

    return callback.invoke(script, thisObj, jArgs);
  }

  @Override
  public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
    throw new IllegalStateException("Native Java function wrapper cannot be instantiated");
  }
}
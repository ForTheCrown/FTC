package net.forthecrown.scripts.module;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Symbol;

public class ScriptableModule implements JsModule {

  private final Scriptable script;

  public ScriptableModule(Scriptable scriptable) {
    this.script = scriptable;
  }

  public static Object getProperty(Scriptable scriptable, Object y) {
    if (y instanceof Integer i) {
      return ScriptableObject.getProperty(scriptable, i);
    }

    if (y instanceof Symbol sym) {
      return ScriptableObject.getProperty(scriptable, sym);
    }

    return ScriptableObject.getProperty(scriptable, String.valueOf(y));
  }

  @Override
  public Scriptable getSelfObject(Scriptable scope) {
    return script;
  }
}

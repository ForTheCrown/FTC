package net.forthecrown.scripts.module;

import net.forthecrown.scripts.Script;
import org.mozilla.javascript.Scriptable;

public interface JsModule {

  Scriptable getSelfObject(Scriptable scope);

  default void onImportFail(Script script) {

  }
}

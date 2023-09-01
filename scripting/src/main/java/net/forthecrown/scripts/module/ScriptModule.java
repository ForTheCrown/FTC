package net.forthecrown.scripts.module;

import net.forthecrown.scripts.Script;

public class ScriptModule extends ScriptableModule {

  private final Script script;

  public ScriptModule(Script script) {
    super(script.getScriptObject());
    this.script = script;
  }

  @Override
  public void onImportFail(Script importedInto) {
    var parent = script.getParent();

    if (parent != null) {
      parent.removeChild(script);
    }

    script.close();
  }
}

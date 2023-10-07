package net.forthecrown.scripts.module;

import net.forthecrown.scripts.Script;
import net.forthecrown.scripts.ScriptObject;

public class ScriptModule extends ScriptableModule {

  private final Script script;

  public ScriptModule(Script script) {
    super(new ScriptObject(script));
    this.script = script;
  }

  @Override
  public void close() {
    script.close();
  }
}

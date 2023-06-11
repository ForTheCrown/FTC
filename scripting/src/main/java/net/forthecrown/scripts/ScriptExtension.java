package net.forthecrown.scripts;

import lombok.Getter;

@Getter
public abstract class ScriptExtension {

  private Script script;

  final void setScript(Script script) {
    if (script == null && this.script != null) {
      onUnbind(this.script);
    } else if (script != null && this.script == null) {
      onBind(script);
    }

    this.script = script;
  }

  protected void onBind(Script script) {

  }

  protected void onUnbind(Script script) {

  }

  protected void onScriptCompile() {

  }

  protected void onScriptClose() {

  }
}
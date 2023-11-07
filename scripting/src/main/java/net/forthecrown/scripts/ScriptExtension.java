package net.forthecrown.scripts;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

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

  protected void onBind(@NotNull Script script) {

  }

  protected void onUnbind(@NotNull Script script) {

  }

  protected void onScriptCompile(Script script) {

  }

  protected void onScriptClose(Script script) {

  }
}
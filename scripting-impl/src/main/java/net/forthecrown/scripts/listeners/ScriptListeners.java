package net.forthecrown.scripts.listeners;

import static net.forthecrown.events.Events.register;

import net.forthecrown.scripts.ScriptingPlugin;

public final class ScriptListeners {
  private ScriptListeners() {}

  public static void registerAll(ScriptingPlugin plugin) {
    register(new ServerLoadListener(plugin));
  }
}

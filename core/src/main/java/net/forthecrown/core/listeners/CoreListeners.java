package net.forthecrown.core.listeners;

import static net.forthecrown.events.Events.register;

public final class CoreListeners {
  private CoreListeners() {}

  public static void registerAll() {
    register(new ServerLoadListener());
    register(new PlayerLoggingListener());
    register(new PlayerTeleportListener());
  }
}
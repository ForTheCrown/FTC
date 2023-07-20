package net.forthecrown.antigrief.listeners;

import static net.forthecrown.events.Events.register;

public final class AntiGriefListeners {
  private AntiGriefListeners() {}

  public static void registerAll() {
    register(new ChannelMessageListener());
    register(new ChatListener());
    register(new ServerLoadListener());
  }
}
package net.forthecrown.antigrief.listeners;

import static net.forthecrown.events.Events.register;

import net.forthecrown.antigrief.AntiGriefPlugin;

public final class AntiGriefListeners {
  private AntiGriefListeners() {}

  public static void registerAll(AntiGriefPlugin plugin) {
    register(new ChannelMessageListener());
    register(new ChatListener());
    register(new EavesDropperListener());
    register(new JailListener());
    register(new JoinListener());
    register(new LoginListener());
    register(new ServerLoadListener());
    register(new VeinListener(plugin));
  }
}
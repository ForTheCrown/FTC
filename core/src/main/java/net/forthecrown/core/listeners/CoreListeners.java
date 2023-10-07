package net.forthecrown.core.listeners;

import static net.forthecrown.events.Events.register;

import net.forthecrown.core.CorePlugin;

public final class CoreListeners {
  private CoreListeners() {}

  public static void registerAll(CorePlugin plugin) {
    register(new AdminBroadcastListener());
    register(new AltLoginListener(plugin));
    register(new AnvilListener(plugin));
    register(new ChatHandleListener());
    register(new DepositListener(plugin));
    register(new GamemodeListener());
    register(new HopperListener(plugin));
    register(new IgnoreListListener());
    register(new MobHealthBar(plugin));
    register(new NoCopiesListener());
    register(new PlayerLoggingListener(plugin));
    register(new PlayerTeleportListener());
    register(new ServerListener());
    register(new ServerPingListener());
    register(new SignOwnershipListener());
    register(new SmokeBomb());
    register(new TextDecorationListener());
    register(new TrapDoorListener());
    register(new WorldAccessListener(plugin));
  }
}
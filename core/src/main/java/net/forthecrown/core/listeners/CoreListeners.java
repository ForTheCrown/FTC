package net.forthecrown.core.listeners;

import static net.forthecrown.events.Events.register;

import net.forthecrown.packet.PacketListeners;
import net.forthecrown.packet.PacketRenderingService;

public final class CoreListeners {
  private CoreListeners() {}

  public static void registerAll() {
    register(new ServerLoadListener());
    register(new PlayerLoggingListener());
    register(new PlayerTeleportListener());
    register(new ServerPingListener());
    register(new AdminBroadcastListener());
    register(new SignOwnershipListener());

    register(new ChatHandleListener());
    register(new TextDecorationListener());
    register(new IgnoreListListener());

    PacketRenderingService service = PacketListeners.listeners().getRenderingService();
    service.getSignRenderers().register("test", new SignRenderTest());
  }
}
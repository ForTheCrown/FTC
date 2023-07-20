package net.forthecrown.packet;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface PacketListeners {

  static PacketListeners listeners() {
    return ListenersImpl.getListeners();
  }

  PacketRenderingService getRenderingService();

  void register(Object o);

  void unregister(Object o);

  @Internal
  void inject(Player player);

  @Internal
  void uninject(Player player);
}

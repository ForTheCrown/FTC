package net.forthecrown.packet;

import net.forthecrown.BukkitServices;
import net.forthecrown.registry.Registry;

public interface PacketListeners {

  static PacketListeners listeners() {
    return BukkitServices.load(PacketListeners.class).orElseGet(NopListeners::nop);
  }

  Registry<SignRenderer> getSignRenderers();

  Registry<EntityRenderer> getEntityRenderers();

  void register(Object o);

  void unregister(Object o);
}

package net.forthecrown.user;

import net.forthecrown.BukkitServices;

public interface PacketListeners {

  PacketListeners NOP = new PacketListeners() {
    @Override
    public void register(Object listener) {

    }

    @Override
    public void unregister(Object listener) {

    }
  };

  static PacketListeners listeners() {
    return BukkitServices.load(PacketListeners.class).orElse(NOP);
  }

  void register(Object listener);

  void unregister(Object listener);
}

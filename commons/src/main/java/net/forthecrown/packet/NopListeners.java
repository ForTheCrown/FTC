package net.forthecrown.packet;

import lombok.Getter;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;

@Getter
class NopListeners implements PacketListeners {

  static NopListeners nop;

  private final Registry<SignRenderer> signRenderers = Registries.newRegistry();
  private final Registry<EntityRenderer> entityRenderers = Registries.newRegistry();

  static NopListeners nop() {
    return nop == null ? (nop = new NopListeners()) : nop;
  }

  @Override
  public void register(Object o) {

  }

  @Override
  public void unregister(Object o) {

  }
}

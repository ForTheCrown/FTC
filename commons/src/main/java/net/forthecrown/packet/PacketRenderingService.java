package net.forthecrown.packet;

import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;

public interface PacketRenderingService {

  Registry<SignRenderer> getSignRenderers();

  Registry<EntityRenderer> getEntityRenderers();
}

package net.forthecrown.packet;

import java.util.Objects;
import lombok.Getter;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
class NopListeners implements PacketListeners {

  static NopListeners nop;

  private final Registry<SignRenderer> signRenderers = Registries.newRegistry();
  private final Registry<EntityRenderer> entityRenderers = Registries.newRegistry();

  static NopListeners nop() {
    return nop == null ? (nop = new NopListeners()) : nop;
  }

  @Override
  public void setEntityDisplay(
      @NotNull Entity entity,
      @NotNull Player viewer,
      @Nullable Component text
  ) {
    Objects.requireNonNull(entity, "Null entity");
    Objects.requireNonNull(viewer, "Null viewer");
  }

  @Override
  public void register(Object o) {

  }

  @Override
  public void unregister(Object o) {

  }
}

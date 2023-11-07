package net.forthecrown.packet;

import net.forthecrown.BukkitServices;
import net.forthecrown.registry.Registry;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PacketListeners {

  static PacketListeners listeners() {
    return BukkitServices.load(PacketListeners.class).orElseGet(NopListeners::nop);
  }

  /**
   * Sets the 'display text' a specified player will see for the specified entity.
   * <p>
   * "display text" means, for most entities, their customName field. Except for {@link TextDisplay}
   * entities, they will have their {@link TextDisplay#text(Component)} field changed.
   *
   * @param entity Entity to change the display text of
   * @param viewer Player viewing the entity
   * @param text Text to change it to, or {@code null}, to re-sync entity display text
   */
  void setEntityDisplay(@NotNull Entity entity, @NotNull Player viewer, @Nullable Component text);

  Registry<SignRenderer> getSignRenderers();

  Registry<EntityRenderer> getEntityRenderers();

  void register(Object o);

  void unregister(Object o);
}

package net.forthecrown.registry;

import org.jetbrains.annotations.Nullable;

/**
 * Object bound to a registry entry
 * @param <V> Self type
 */
public interface RegistryBound<V extends RegistryBound<V>> {

  /**
   * Sets the object's registry entry.
   * <p>
   * If the specified {@code holder} is null, it means the object has been removed from
   * the registry
   *
   * @param holder Object's registry entry
   */
  void setHolder(@Nullable Holder<V> holder);

  /**
   * Gets the object's registry entry
   * @return Registry entry, {@code null}, if not bound a registry
   */
  @Nullable
  Holder<V> getHolder();

  /**
   * Gets the object's registry entry ID
   * @return Holder ID, or {@code -1}, if not bound to a registry
   */
  default int getHolderId() {
    return getHolder() == null ? -1 : getHolder().getId();
  }

  /**
   * Gets the object's registry entry key
   * @return Holder key, or {@code null}, if not bound to a registry
   */
  default String getHolderKey() {
    return getHolder() == null ? null : getHolder().getKey();
  }
}
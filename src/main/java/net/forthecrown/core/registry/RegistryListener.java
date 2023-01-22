package net.forthecrown.core.registry;

/**
 * A simple interface that can be attached to registries to listen for value
 * registrations
 */
public interface RegistryListener<V> {

  /**
   * Called when a value is registered
   * @param value The registered entry
   */
  void onRegister(Holder<V> value);

  /**
   * Called when a value is removed from the registry
   * @param value The unregistered entry
   */
  void onUnregister(Holder<V> value);
}
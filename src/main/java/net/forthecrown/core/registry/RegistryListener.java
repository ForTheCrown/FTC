package net.forthecrown.core.registry;

public interface RegistryListener<V> {
  void onRegister(Holder<V> value);
  void onUnregister(Holder<V> value);
}
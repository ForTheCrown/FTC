package net.forthecrown.core.user;

import com.google.common.base.Strings;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.registry.RegistryListener;
import net.forthecrown.user.ComponentName;
import net.forthecrown.user.UserComponent;

public final class Components {
  private Components() {}

  public static final Registry<ComponentFactory<?>> REGISTRY = Registries.newRegistry();

  static {
    REGISTRY.setListener(new RegistryListener<>() {
      @Override
      public void onRegister(Holder<ComponentFactory<?>> value) {
        value.getValue().id = value.getId();
      }

      @Override
      public void onUnregister(Holder<ComponentFactory<?>> value) {
        value.getValue().id = -1;
      }
    });
  }

  public static <T extends UserComponent> ComponentFactory<T> getFactory(Class<T> type) {
    String name = findComponentName(type, true);
    ComponentFactory<T> factory = null;

    if (name != null) {
      factory = (ComponentFactory<T>) REGISTRY.orNull(name);

      if (factory != null) {
        return factory;
      }
    }

    factory = classLookup(type);

    if (factory != null) {
      return factory;
    }

    return createFactory(type);
  }

  private static <T extends UserComponent> ComponentFactory<T> classLookup(Class<T> type) {
    for (ComponentFactory<?> n : REGISTRY) {
      if (type.isAssignableFrom(n.getType())) {
        return (ComponentFactory<T>) n;
      }
    }

    return null;
  }

  public static <T extends UserComponent> ComponentFactory<T> createFactory(Class<T> type) {
    String name = findComponentName(type, false);
    ComponentFactory<T> factory = new ComponentFactory<>(type);

    REGISTRY.register(name, factory);
    return factory;
  }

  public static String findComponentName(Class<?> type, boolean allowNull) {
    ComponentName name = type.getAnnotation(ComponentName.class);
    String result = "";

    if (name != null) {
      result = name.value();

      if (Strings.isNullOrEmpty(result)) {
        if (allowNull) {
          return null;
        }

        throw new IllegalStateException(
            "Class " + type + " had empty ComponentName annotation value"
        );
      }

      return result;
    }

    result = type.getSimpleName().replace("User", "");
    char first = result.charAt(0);

    return Character.toLowerCase(first) + result.substring(1);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static ComponentFactory<UserComponent> createUnknown(String id) {
    ComponentFactory factory = new ComponentFactory(UnknownComponent.class);
    REGISTRY.register(id, factory);
    return factory;
  }
}
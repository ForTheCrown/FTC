package net.forthecrown.core.user;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
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
    String name = findComponentName(type, true).left();
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
    ObjectBooleanPair<String> metadataPair = findComponentName(type, false);

    ComponentFactory<T> factory = new ComponentFactory<>(type);
    factory.setRedirectAlts(metadataPair.rightBoolean());

    REGISTRY.register(metadataPair.left(), factory);
    return factory;
  }

  public static ObjectBooleanPair<String> findComponentName(Class<?> type, boolean allowNull) {
    ComponentName name = type.getAnnotation(ComponentName.class);

    if (name != null) {
      String nameValue = name.value();
      boolean redirectAlts = name.redirectAlts();

      if (Strings.isNullOrEmpty(nameValue)) {
        if (allowNull) {
          return ObjectBooleanPair.of(null, false);
        }

        throw new IllegalStateException(
            "Class " + type + " had empty ComponentName annotation value"
        );
      }

      return ObjectBooleanPair.of(nameValue, redirectAlts);
    }

    String result = type.getSimpleName()
        .replace("User", "")
        .replace("Impl", "");

    char first = result.charAt(0);

    return ObjectBooleanPair.of(
        Character.toLowerCase(first) + result.substring(1),
        true
    );
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static ComponentFactory<UserComponent> createUnknown(String id) {
    ComponentFactory factory = new ComponentFactory(UnknownComponent.class);
    REGISTRY.register(id, factory);
    return factory;
  }
}
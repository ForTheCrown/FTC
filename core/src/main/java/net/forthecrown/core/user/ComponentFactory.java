package net.forthecrown.core.user;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.RegistryBound;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;

public class ComponentFactory<T extends UserComponent>
    implements RegistryBound<ComponentFactory<T>>
{

  private final ConstructorWrapper<T> constructor;

  @Getter
  private final Class<T> type;

  @Getter @Setter
  private Holder<ComponentFactory<T>> holder;

  @Setter @Getter
  private boolean redirectAlts;

  public ComponentFactory(Class<T> type) {
    this.type = type;

    if (type.isInterface()
        || Modifier.isAbstract(type.getModifiers())
        || type.isSynthetic()
        || type.isAnonymousClass()
    ) {
      throw new IllegalStateException(
          "Class " + type + " cannot be a UserComponent, only regular classes can"
      );
    }

    this.constructor = findWrapper();
    this.redirectAlts = true;
  }

  public T newComponent(User user) {
    try {
      return constructor.newInstance(user);
    } catch (ReflectiveOperationException exc) {
      throw new RuntimeException(
          exc instanceof InvocationTargetException
              ? exc.getCause()
              : exc
      );
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private ConstructorWrapper<T> findWrapper() {
    Constructor<?>[] constructors = type.getDeclaredConstructors();

    Constructor empty   = null;
    Constructor regular = null;

    for (int i = 0; i < constructors.length; i++) {
      Constructor<?> ctor = constructors[i];
      int paramCount = ctor.getParameterCount();

      if (paramCount < 1) {
        empty = ctor;
      }

      if (paramCount > 1) {
        continue;
      }

      Parameter first = ctor.getParameters()[0];

      if (!User.class.isAssignableFrom(first.getType())) {
        continue;
      }

      regular = ctor;
    }

    if (empty == null && regular == null) {
      throw new IllegalStateException(
          "Class '%s' has no empty constructor or single parameter user constructor!"
              .formatted(type)
      );
    }

    if (regular != null) {
      return new RegularConstructorWrapper<>(regular);
    } else {
      return new EmptyConstructorWrapper<>(regular);
    }
  }

  interface ConstructorWrapper<T extends UserComponent> {
    T newInstance(User user) throws ReflectiveOperationException;
  }

  record EmptyConstructorWrapper<T extends UserComponent>(Constructor<T> ctor)
      implements ConstructorWrapper<T>
  {

    @Override
    public T newInstance(User user) throws ReflectiveOperationException {
      return ctor.newInstance();
    }
  }

  record RegularConstructorWrapper<T extends UserComponent>(Constructor<T> ctor)
      implements ConstructorWrapper<T>
  {

    @Override
    public T newInstance(User user) throws ReflectiveOperationException {
      return ctor.newInstance(user);
    }
  }
}
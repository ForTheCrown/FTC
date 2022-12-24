package net.forthecrown.dungeons.level.room;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import lombok.Getter;
import net.forthecrown.utils.Util;

@Getter
public class RoomComponentType<T extends RoomComponent> {
  private final Class<T> type;
  private final Constructor<T> constructor;

  private int index = -1;
  private String key = null;

  public RoomComponentType(Class<T> type) {
    this.type = type;
    this.constructor = findConstructor(type);
  }

  void onRegister(String key, int index) {
    if (index != -1) {
      throw Util.newException("Component type %s already registered??", type);
    }

    this.key = key;
    this.index = index;
  }

  public T newInstance() {
    try {
      return constructor.newInstance();
    } catch (Throwable exc) {
      if (exc instanceof InvocationTargetException e) {
        exc = e.getCause();
      }
      throw new IllegalStateException("Couldn't instantiate " + type, exc);
    }
  }

  private static <T> Constructor<T> findConstructor(Class<T> type) {
    try {
      return type.getConstructor();
    } catch (ReflectiveOperationException exc) {
      throw new IllegalStateException(
          "Room component must have an empty constructor! "
              + "Didn't find one in: " + type,
          exc
      );
    }
  }
}
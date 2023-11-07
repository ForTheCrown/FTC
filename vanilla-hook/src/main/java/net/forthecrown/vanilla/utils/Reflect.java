package net.forthecrown.vanilla.utils;

import java.lang.reflect.Field;

public final class Reflect {
  private Reflect() {}

  public static <T> T getField(Object o, String fieldName) {
    try {
      Field f = o.getClass().getDeclaredField(fieldName);
      f.setAccessible(true);
      return (T) f.get(o);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
}

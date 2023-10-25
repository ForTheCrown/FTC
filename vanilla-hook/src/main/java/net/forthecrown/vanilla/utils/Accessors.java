package net.forthecrown.vanilla.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;

public class Accessors {

  public static final int ACCESSOR_CUSTOM_NAME = 2;
  public static final int ACCESSOR_TEXT_DISPLAY_TEXT = 23;

  private static final Table<Class<? extends Entity>, Integer, EntityDataAccessor<?>> cache
      = HashBasedTable.create();

  public static <T> EntityDataAccessor<T> find(
      int accessorId,
      Class<? extends Entity> entityClass
  ) {
    var cached = cache.get(entityClass, accessorId);
    if (cached != null) {
      return (EntityDataAccessor<T>) cached;
    }

    Field[] fields = entityClass.getDeclaredFields();

    for (Field f : fields) {
      if (!Modifier.isStatic(f.getModifiers()) || !Modifier.isFinal(f.getModifiers())) {
        continue;
      }

      if (!EntityDataAccessor.class.isAssignableFrom(f.getType())) {
        continue;
      }

      try {
        f.setAccessible(true);
        EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) f.get(null);

        if (accessor.getId() != accessorId) {
          continue;
        }

        cache.put(entityClass, accessorId, accessor);
        return (EntityDataAccessor<T>) accessor;
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    return null;
  }
}

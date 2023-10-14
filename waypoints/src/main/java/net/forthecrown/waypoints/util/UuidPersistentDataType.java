package net.forthecrown.waypoints.util;

import java.util.UUID;
import net.forthecrown.utils.io.FtcCodecs;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public enum UuidPersistentDataType implements PersistentDataType<int[], UUID> {
  INSTANCE;

  @Override
  public @NotNull Class<int[]> getPrimitiveType() {
    return int[].class;
  }

  @Override
  public @NotNull Class<UUID> getComplexType() {
    return UUID.class;
  }

  @Override
  public int @NotNull [] toPrimitive(
      @NotNull UUID complex,
      @NotNull PersistentDataAdapterContext context
  ) {
    return FtcCodecs.uuidToIntArray(complex);
  }

  @Override
  public @NotNull UUID fromPrimitive(
      int @NotNull [] primitive,
      @NotNull PersistentDataAdapterContext context
  ) {
    return FtcCodecs.uuidFromIntArray(primitive);
  }
}

package net.forthecrown.dungeons.level;

import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.core.registry.RegistryKey;
import net.forthecrown.dungeons.DungeonManager;
import net.forthecrown.dungeons.level.room.RoomFlag;
import net.forthecrown.dungeons.level.room.RoomType;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.StringTag;
import org.jetbrains.annotations.Nullable;

public final class Pieces {
  private Pieces() {}

  @SuppressWarnings("rawtypes")
  public static @Nullable PieceType load(BinaryTag t) {
    RegistryKey key = RegistryKey.load(t);

    if (key == null) {
      return null;
    }

    Registry<PieceType> registry = DungeonManager.getDungeons()
        .getTypeRegistries()
        .orNull(key.getRegistry());

    if (registry == null) {
      return null;
    }

    return registry.orNull(key.getValue());
  }

  @SuppressWarnings("rawtypes")
  public static StringTag save(PieceType type) {
    var registry = DungeonManager.getDungeons()
        .getTypeRegistries();

    for (var r : registry.entries()) {
      var holder = r.getValue()
          .getHolderByValue(type)
          .map(holder1 -> RegistryKey.of(r.getKey(), holder1.getKey()).save());

      if (holder.isEmpty()) {
        continue;
      }

      return holder.get();
    }

    return null;
  }

  public static Holder<RoomType> getRoot() {
    var reg = DungeonManager.getDungeons().getRoomTypes();

    for (var e : reg.entries()) {
      if (e.getValue().hasFlag(RoomFlag.ROOT)) {
        return e;
      }
    }

    throw new IllegalStateException("No root room set");
  }

}
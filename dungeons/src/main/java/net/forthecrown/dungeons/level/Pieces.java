package net.forthecrown.dungeons.level;

import com.google.common.base.Preconditions;
import net.forthecrown.dungeons.DungeonManager;
import net.forthecrown.dungeons.level.room.RoomFlag;
import net.forthecrown.dungeons.level.room.RoomType;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.StringTag;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registry;
import org.jetbrains.annotations.Nullable;

public final class Pieces {
  private Pieces() {}

  @SuppressWarnings("rawtypes")
  public static @Nullable PieceType load(BinaryTag t) {
    String str = t.asString().value();
    String[] split = str.split("::");

    Preconditions.checkArgument(split.length == 2, "Invalid type reference: '%s'", str);

    String registryKey = split[0];
    String typeKey = split[1];

    Registry<PieceType> registry = DungeonManager.getDungeons()
        .getTypeRegistries()
        .orNull(registryKey);

    if (registry == null) {
      return null;
    }

    return registry.orNull(typeKey);
  }

  @SuppressWarnings("rawtypes")
  public static StringTag save(PieceType type) {
    var registry = DungeonManager.getDungeons()
        .getTypeRegistries();

    for (var r : registry.entries()) {
      var holder = r.getValue()
          .getHolderByValue(type)
          .map(holder1 -> BinaryTags.stringTag(r.getKey() + "::" + holder1.getKey()));

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
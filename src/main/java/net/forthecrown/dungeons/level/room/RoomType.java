package net.forthecrown.dungeons.level.room;

import java.util.Map;
import lombok.Getter;
import net.forthecrown.dungeons.level.PieceStyle;
import net.forthecrown.dungeons.level.PieceType;
import net.minecraft.nbt.CompoundTag;

@Getter
public class RoomType extends PieceType<DungeonRoom> {

  private final int flags;

  public RoomType(String structureName,
                  Map<PieceStyle, String> variants,
                  int flags
  ) {
    super(structureName, variants);
    this.flags = flags;
  }

  public boolean hasFlags(int flags) {
    return (this.flags & flags) == flags;
  }

  @Override
  public DungeonRoom create() {
    return new DungeonRoom(this);
  }

  @Override
  public DungeonRoom load(CompoundTag tag) {
    return new DungeonRoom(this, tag);
  }
}
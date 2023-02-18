package net.forthecrown.dungeons.level.room;

import lombok.Getter;
import net.forthecrown.dungeons.level.PieceType;
import net.minecraft.nbt.CompoundTag;

@Getter
public class RoomType extends PieceType<RoomPiece> {

  private final int flags;

  public RoomType(String structureName, int flags) {
    super(structureName);
    this.flags = flags;
  }

  public boolean hasFlags(int flags) {
    return (this.flags & flags) == flags;
  }

  @Override
  public RoomPiece create() {
    return new RoomPiece(this);
  }

  @Override
  public RoomPiece load(CompoundTag tag) {
    return new RoomPiece(this, tag);
  }
}
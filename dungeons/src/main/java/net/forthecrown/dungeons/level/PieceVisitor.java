package net.forthecrown.dungeons.level;

import net.forthecrown.dungeons.level.gate.GatePiece;
import net.forthecrown.dungeons.level.room.RoomPiece;

public interface PieceVisitor {

  Result onGate(GatePiece gate);

  Result onRoom(RoomPiece room);

  default void onChildrenStart(DungeonPiece piece) {

  }

  default void onChildrenEnd(DungeonPiece piece) {

  }

  default void onPieceStart(DungeonPiece piece) {

  }

  default void onPieceEnd(DungeonPiece piece) {

  }

  enum Result {
    STOP,
    CONTINUE,
    SKIP_CHILDREN
  }
}
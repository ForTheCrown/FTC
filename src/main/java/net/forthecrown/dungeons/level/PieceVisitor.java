package net.forthecrown.dungeons.level;

import net.forthecrown.dungeons.level.gate.DungeonGate;

public interface PieceVisitor {
    Result onGate(DungeonGate gate);
    Result onRoom(DungeonRoom room);

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
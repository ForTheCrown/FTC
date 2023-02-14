package net.forthecrown.dungeons.level.gate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.dungeons.level.room.RoomPiece;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@RequiredArgsConstructor
public class PathData {

  private final RoomPiece connectorRoot;
  private int connectorDepth;
}
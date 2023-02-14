package net.forthecrown.dungeons.level.placement;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.PieceVisitor;
import net.forthecrown.dungeons.level.gate.GatePiece;
import net.forthecrown.dungeons.level.room.RoomPiece;
import net.forthecrown.structure.BlockProcessors;
import net.forthecrown.structure.StructurePlaceConfig;
import net.forthecrown.structure.StructurePlaceConfig.Builder;
import net.forthecrown.utils.math.Transform;
import org.apache.logging.log4j.Logger;

public class RoomPlacingVisitor implements PieceVisitor {

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter @Setter
  private PostProcessCollector collector;

  @Getter
  private int placementCounter = 0;

  @Override
  public Result onGate(GatePiece gate) {
    place(gate);
    return Result.CONTINUE;
  }

  @Override
  public Result onRoom(RoomPiece room) {
    place(room);
    return Result.CONTINUE;
  }

  private void place(DungeonPiece piece) {
    var struct = piece.getStructure();

    if (struct == null) {
      LOGGER.error(
          "Cannot place piece {}, at {}, no structure with name {}",
          piece, piece.getPivotPosition(),
          piece.getType().getStructureName()
      );

      return;
    }

    Builder builder = StructurePlaceConfig.builder()
        .placeEntities(true)
        .pos(piece.getPivotPosition())
        .transform(Transform.rotation(piece.getRotation()))
        .paletteName(piece.getPaletteName())
        .addNonNullProcessor()
        .addRotationProcessor()
        .addProcessor(BlockProcessors.IGNORE_AIR);

    var config = builder.build();
    struct.place(config);

    if (collector != null) {
      struct.getFunctions().forEach(func -> {
        if (!func.getFunctionKey().startsWith("post/")) {
          return;
        }

        var info = func.withOffset(
            config.getTransform().apply(func.getOffset())
        );

        collector.addMarker(info);
      });
    }

    placementCounter++;
  }
}
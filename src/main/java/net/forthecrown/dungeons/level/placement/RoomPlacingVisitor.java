package net.forthecrown.dungeons.level.placement;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public class RoomPlacingVisitor implements PieceVisitor {

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  private final LevelPlacement placement;
  private final Lock lock = new ReentrantLock();

  @Getter
  private AtomicInteger placementCounter = new AtomicInteger(0);
  private int roomCount = 0;
  private boolean finishCalled = false;

  @Override
  public Result onGate(GatePiece gate) {
    addPlaceTask(gate);
    return Result.CONTINUE;
  }

  @Override
  public Result onRoom(RoomPiece room) {
    addPlaceTask(room);
    return Result.CONTINUE;
  }

  private void addPlaceTask(DungeonPiece piece) {
    placement.getExecutorService().execute(() -> place(piece));
  }

  public synchronized boolean isFinished() {
    return placementCounter.get() >= roomCount;
  }

  private void onPlaced() {
    lock.lock();

    placementCounter.incrementAndGet();
    LOGGER.debug("Placed room, placementCount={}", placementCounter);

    if (isFinished() && !finishCalled) {
      LOGGER.debug("isFinished() == true");

      finishCalled = true;
      placement.onPlacementsFinished();
    }

    lock.unlock();
  }

  private void place(DungeonPiece piece) {
    var struct = piece.getStructure();

    if (struct == null) {
      LOGGER.error(
          "Cannot place piece {}, at {}, no structure with name {}",
          piece,
          piece.getPivotPosition(),
          piece.getType().getStructureName()
      );

      return;
    }

    roomCount++;
    var center = piece.getBounds().center();
    var random = placement.getRandom();

    var source = placement.getBiomeSource();
    var biome  = source.findBiome(center);

    Builder builder = StructurePlaceConfig.builder()
        .pos(piece.getPivotPosition())
        .transform(Transform.rotation(piece.getRotation()))

        .buffer(placement.getBuffer())
        .entitySpawner(placement.getEntityPlacement())

        .paletteName(piece.getPaletteName(biome))

        .addNonNullProcessor()
        .addRotationProcessor()
        .addProcessor(BlockProcessors.IGNORE_AIR)
        .addProcessor(BlockProcessors.rot(placement, random));

    var config = builder.build();
    struct.place(config);

    struct.getFunctions().forEach(func -> {
      if (!func.getFunctionKey().startsWith("post/")) {
        return;
      }

      var info = func.withOffset(
          config.getTransform().apply(func.getOffset())
      );

      placement.addMarker(info);
    });

    onPlaced();
  }
}
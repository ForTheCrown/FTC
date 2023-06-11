package net.forthecrown.dungeons.level.generator;

import static net.forthecrown.dungeons.level.generator.StepResult.FAILED;
import static net.forthecrown.dungeons.level.generator.StepResult.MAX_DEPTH;
import static net.forthecrown.dungeons.level.generator.StepResult.MAX_SECTION_DEPTH;
import static net.forthecrown.dungeons.level.generator.StepResult.SUCCESS;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Comparator;
import java.util.Deque;
import java.util.IntSummaryStatistics;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.forthecrown.Loggers;
import net.forthecrown.dungeons.DungeonManager;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.PieceVisitor;
import net.forthecrown.dungeons.level.Pieces;
import net.forthecrown.dungeons.level.gate.GatePiece;
import net.forthecrown.dungeons.level.gate.GateType;
import net.forthecrown.dungeons.level.gate.Gates;
import net.forthecrown.dungeons.level.room.RoomFlag;
import net.forthecrown.dungeons.level.room.RoomPiece;
import net.forthecrown.dungeons.level.room.RoomType;
import net.forthecrown.registry.Holder;
import net.forthecrown.utils.math.Transform;
import org.apache.commons.lang3.Range;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Getter
public class TreeGenerator {

  private static final Logger LOGGER = Loggers.getLogger();

  public static final int MAX_OVERLAP = 2;

  public static final int MAX_FAILED_STEPS = 55;

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  private final TreeGeneratorConfig config;
  private final Range<Integer> depthRange;

  private DungeonLevel level;
  private boolean finished = false;

  private final Deque<PieceGenerator> genQueue = new LinkedList<>();

  private final List<LevelGenResult> generationResults = new ObjectArrayList<>();

  private int failedSteps = 0;

  /* ----------------------------- CONSTRUCTOR ------------------------------ */

  public TreeGenerator(TreeGeneratorConfig config) {
    this.config = config;
    depthRange = Range.between(config.getMinDepth(), config.getMaxDepth());
  }

  /* ----------------------------- STATIC METHODS ------------------------------ */

  public static CompletableFuture<DungeonLevel> generateAsync(
      TreeGeneratorConfig config
  ) {
    var exc = DungeonManager.getDungeons().getExecutorService();

    return CompletableFuture.supplyAsync(() -> {
      return new TreeGenerator(config).generate();
    }, exc)
        .whenComplete((level1, throwable) -> {
          if (throwable != null) {
            LOGGER.error("Error generating level", throwable);
          }
        });
  }

  /* ----------------------------- METHODS ------------------------------ */

  public DungeonLevel generate() {
    createPotentialLevels();

    generationResults.sort(Comparator.naturalOrder());
    LevelGenResult first = generationResults.get(0);

    if (!isValidLevel(first)) {
      throw new IllegalStateException("NO VALID LEVEL");
    }

    var level = first.level();
    generationResults.clear();

    return level;
  }

  private void createPotentialLevels() {
    int created = 0;

    while (created < config.getPotentialLevels()) {
      reset();

      while (!isFinished()) {
        step();
      }

      // Recursively cut off all connector rooms that lead
      // to nothing
      while (removeDeadEnds(level.getRoot())) {
      }

      var result = compileResult();
      addBossRoom(result);
      addDecorateGates(result);

      generationResults.add(result);
      ++created;
    }
  }

   boolean isValidLevel(LevelGenResult result) {
    if (result.nonConnectorRooms < config.getRequiredRooms()
        || !depthRange.contains(result.endDepthStats.getMax())
        || result.level.getBossRoom() == null
    ) {
      return false;
    }

    return depthRange.contains(result.endDepthStats.getMin())
        || depthRange.contains((int) result.endDepthStats.getAverage());
  }

  private LevelGenResult compileResult() {
    ValidationVisitor walker = new ValidationVisitor();
    level.getRoot().visit(walker);

    walker.endGates.sort((o1, o2) -> Integer.compare(o2.getDepth(), o1.getDepth()));
    var stats = walker.endGates
        .stream()
        .mapToInt(DungeonPiece::getDepth)
        .summaryStatistics();

    var genResult = new LevelGenResult(
        level,
        walker.nonConnectorRooms,
        stats,
        walker.endGates,
        walker.totalRoomCount,
        walker.closedEndConnectors
    );

    return genResult;
  }

  public void reset() {
    finished = false;
    genQueue.clear();

    RoomPiece root = Pieces.getRoot().getValue().create();
    root.apply(Transform.offset(config.getLocation()));

    level = new DungeonLevel();
    level.addPiece(root);

    var gates = Gates.createExitGates(
        root,
        root.getType().getGates(),
        config.getRandom()
    );

    genQueue.addAll(
        gates.stream()
            .filter(gatePiece -> {
              if (!gatePiece.getType().isOpenable()) {
                LOGGER.warn(
                    "Tried to add gate type {} to root room, not openable",
                    gatePiece.getType().getStructureName()
                );

                return false;
              }

              return true;
            })

            .map(gate -> new PieceGenerator(SectionType.CONNECTOR, this, gate, null))
            .toList()
    );

    if (genQueue.isEmpty()) {
      throw new IllegalStateException(
          "Couldn't add any openable gates to root room"
      );
    }
  }

  public boolean isValidPlacement(DungeonPiece piece) {
    var bb = piece.getBounds();
    var intersecting = level.getIntersecting(bb);

    if (intersecting.isEmpty()) {
      return true;
    }

    for (var p : intersecting) {
      var intersection = p.getBounds().intersection(bb).size();

      if (intersection.x() > MAX_OVERLAP
          || intersection.y() > MAX_OVERLAP
          || intersection.z() > MAX_OVERLAP
      ) {
        return false;
      }
    }

    return true;
  }

  public void step() {
    PieceGenerator section = genQueue.poll();

    if (section == null) {
      finished = true;
      return;
    }

    var gate = section.getOrigin();
    var result = section.generate();

    switch (result.getResultCode()) {
      case SUCCESS -> {
        result.getSections().forEach(genQueue::addLast);
        section.onSuccess(result.getRoom());
        level.addPiece(result.getRoom());
      }

      case FAILED -> {
        PieceGenerator parent = section.sectionParent();
        failedSteps++;

        if (parent == null || failedSteps > MAX_FAILED_STEPS) {
          gate.setOpen(false);
          return;
        }

        parent.onChildFail((RoomType) gate.getParent().getType());
        genQueue.addFirst(parent);
      }

      case MAX_DEPTH -> {
        var parent = section.getParent();

        if (section.getSectionDepth() < section.getData().getOptimalDepth()
            || (parent != null && parent.getType() == SectionType.CONNECTOR)
        ) {
          var root = section.sectionRoot();

          if (root == null) {
            section.getOrigin().setOpen(false);
            return;
          }

          root.getOrigin().setOpen(true);
          root.getOrigin().clearChildren();

          genQueue.addFirst(root);
        } else if (parent != null && parent.getType() == SectionType.ROOM) {
          section.getOrigin().setOpen(false);
        }
      }

      case MAX_SECTION_DEPTH -> {
        genQueue.addFirst(switchGeneratorType(
            section,
            section.getOrigin(),
            section.getParent()
        ));
      }

      default -> {
        throw new IllegalArgumentException(
            "Invalid return code: " + result.getResultCode()
        );
      }
    }
  }

  PieceGenerator switchGeneratorType(PieceGenerator existing, GatePiece origin,
                                     PieceGenerator parent
  ) {
    var type = existing.getType().next();
    return new PieceGenerator(type, this, origin, parent);
  }

  /* ----------------------------- CLEAN UP ------------------------------ */

  private void addDecorateGates(LevelGenResult result) {
    var root = result.level().getRoot();

    PieceVisitor visitor = new PieceVisitor() {
      @Override
      public Result onGate(GatePiece gate) {
        if (gate.hasChildren()
            || config.getRandom().nextFloat() > config.getDecorateGateRate()
        ) {
          return Result.CONTINUE;
        }

        RoomPiece parent = (RoomPiece) gate.getParent();
        Holder<GateType> gateType = Gates.getClosed(config.getRandom());

        GatePiece g = gateType.getValue().create();
        NodeAlign.align(g, gate.getParentExit(),
            gateType.getValue().getGates().get(0));

        if (!isValidPlacement(g)) {
          return Result.CONTINUE;
        }

        parent.removeChild(gate);
        parent.addChild(g);

        return Result.CONTINUE;
      }

      @Override
      public Result onRoom(RoomPiece room) {
        return Result.CONTINUE;
      }
    };

    root.visit(visitor);
  }

  private boolean removeDeadEnds(RoomPiece root) {
    Set<RoomPiece> endRooms = new ObjectOpenHashSet<>();

    PieceVisitor visitor = new PieceVisitor() {
      @Override
      public Result onGate(GatePiece gate) {
        if (!gate.hasChildren()) {
          endRooms.add((RoomPiece) gate.getParent());
        }

        return Result.CONTINUE;
      }

      @Override
      public Result onRoom(RoomPiece room) {
        return Result.CONTINUE;
      }
    };

    root.visit(visitor);

    endRooms.removeIf(room -> !room.getType().hasFlag(RoomFlag.CONNECTOR));
    boolean result = false;

    for (var d : endRooms) {
      GatePiece parent = (GatePiece) d.getParent();

      if (!d.hasChildren() || canRemoveNode(d)) {
        parent.removeChild(d);
        parent.setOpen(false);
        result = true;
      }
    }

    return result;
  }

  private boolean canRemoveNode(RoomPiece piece) {
    AtomicBoolean atomicBoolean = new AtomicBoolean(true);

    PieceVisitor visitor = new PieceVisitor() {
      @Override
      public Result onGate(GatePiece gate) {
        if (gate.hasChildren()) {
          atomicBoolean.set(false);
          return Result.STOP;
        }

        return Result.CONTINUE;
      }

      @Override
      public Result onRoom(RoomPiece room) {
        if (room.equals(piece)) {
          return Result.CONTINUE;
        }

        atomicBoolean.set(false);
        return Result.STOP;
      }
    };

    piece.visit(visitor);
    return atomicBoolean.get();
  }

  void addBossRoom(LevelGenResult result) {
    RoomType bossRoom = DungeonManager.getDungeons().getRoomTypes()
        .getRandom(
            config.getRandom(),
            roomTypeHolder -> {
              return roomTypeHolder.getValue().hasFlag(RoomFlag.BOSS_ROOM);
            }
        )
        .map(Holder::getValue)
        .orElse(null);

    if (bossRoom == null) {
      LOGGER.warn("Found no boss room to append to dungeon!");
      return;
    }

    for (GatePiece g : result.endGates) {
      RoomPiece room = bossRoom.create();

      NodeAlign.align(
          room,
          g.getTargetGate(),
          bossRoom.getGates().get(0)
      );

      if (isValidPlacement(room)) {
        g.setOpen(true);
        g.addChild(room);
        level.setBossRoom(room);

        return;
      }
    }

    LOGGER.warn("Couldn't find area to place boss room!");
  }

  /* ----------------------------- SUB CLASSES ------------------------------ */

  @Getter
  @Accessors(fluent = true)
  @RequiredArgsConstructor
  final class LevelGenResult implements Comparable<LevelGenResult> {

    private final DungeonLevel level;
    private final int nonConnectorRooms;
    private final IntSummaryStatistics endDepthStats;
    private final List<GatePiece> endGates;
    private final int totalRoomCount;
    private final int closedEndConnectors;

    public int score() {
      if (!isValidLevel(this)) {
        return -1;
      }

      return nonConnectorRooms;
    }

    @Override
    public int compareTo(@NotNull TreeGenerator.LevelGenResult o) {
      return Double.compare(o.score(), this.score());
    }
  }

  static class ValidationVisitor implements PieceVisitor {

    private final List<GatePiece> endGates = new ObjectArrayList<>();

    private int nonConnectorRooms;
    private int totalRoomCount;
    private int closedEndConnectors;

    @Override
    public Result onGate(GatePiece gate) {
      // Close gates that have no children
      if (gate.hasChildren()) {
        return Result.CONTINUE;
      }

      endGates.add(gate);

      if (gate.isOpen()) {
        gate.setOpen(false);
      }

      if (gate.getParent() instanceof RoomPiece room
          && room.getType().hasFlag(RoomFlag.CONNECTOR)
      ) {
        closedEndConnectors++;
      }

      return Result.CONTINUE;
    }

    @Override
    public Result onRoom(RoomPiece room) {
      if (!room.getType().hasFlag(RoomFlag.CONNECTOR)) {
        ++nonConnectorRooms;
      }

      ++totalRoomCount;
      return Result.CONTINUE;
    }
  }
}
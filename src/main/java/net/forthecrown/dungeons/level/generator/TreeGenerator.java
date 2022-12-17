package net.forthecrown.dungeons.level.generator;

import static net.forthecrown.dungeons.level.generator.PieceGenerator.GATE_INDEX_ENTRANCE;
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
import net.forthecrown.core.FTC;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.dungeons.DungeonManager;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.DungeonRoom;
import net.forthecrown.dungeons.level.PieceVisitor;
import net.forthecrown.dungeons.level.Pieces;
import net.forthecrown.dungeons.level.RoomType;
import net.forthecrown.dungeons.level.gate.DungeonGate;
import net.forthecrown.dungeons.level.gate.GateType;
import net.forthecrown.utils.math.Transform;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.Logger;

@Getter
public class TreeGenerator {

  private static final Logger LOGGER = FTC.getLogger();

  public static final int MAX_OVERLAP = 2;

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  private final TreeGeneratorConfig config;
  private final Range<Integer> depthRange;

  private DungeonLevel level;
  private boolean finished = false;

  private final Deque<PieceGenerator> genQueue = new LinkedList<>();

  private final List<LevelGenResult> generationResults = new ObjectArrayList<>();

  private final Comparator<LevelGenResult> comparator = Comparator.comparing(this::isValidLevel)
      .thenComparing((o1, o2) -> {
        // o1 less than o2 = -1
        // o2 less than o1 =  1

        if (o1.totalRoomCount < o2.totalRoomCount
            && o1.closedEndConnectors > o2.closedEndConnectors
        ) {
          return -1;
        }

        if (o2.totalRoomCount < o1.totalRoomCount
            && o1.closedEndConnectors < o2.closedEndConnectors
        ) {
          return 1;
        }

        return 0;
      })

      .thenComparing(
          Comparator.comparing(LevelGenResult::totalRoomCount)
              .reversed()
      )

      .thenComparing(LevelGenResult::closedEndConnectors)
      .thenComparing(value -> ((double) value.nonConnectorRooms) / value.totalRoomCount);

  /* ----------------------------- CONSTRUCTOR ------------------------------ */

  public TreeGenerator(TreeGeneratorConfig config) {
    this.config = config;
    depthRange = Range.between(config.getMinDepth(), config.getMaxDepth());
  }

  /* ----------------------------- STATIC METHODS ------------------------------ */

  public static CompletableFuture<DungeonLevel> generateAsync(TreeGeneratorConfig config) {
    return CompletableFuture.supplyAsync(() -> {
          TreeGenerator generator = new TreeGenerator(config);
          return generator.generate();
        })
        .whenComplete((level1, throwable) -> {
          if (throwable != null) {
            LOGGER.error("Error generating level", throwable);
          }
        });
  }

  /* ----------------------------- METHODS ------------------------------ */

  public DungeonLevel generate() {
    createPotentialLevels();

    generationResults.sort(comparator);

    var level = generationResults.get(0).level();
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

    return new LevelGenResult(
        level,
        walker.nonConnectorRooms,
        stats,
        walker.endGates,
        walker.totalRoomCount,
        walker.closedEndConnectors
    );
  }

  public void reset() {
    finished = false;
    genQueue.clear();

    DungeonRoom root = Pieces.getRoot().getValue().create();
    root.apply(Transform.offset(config.getLocation()));

    level = new DungeonLevel();
    level.addPiece(root);

    var gates = PieceGenerator.createGates(root, root.getType().getGates());
    genQueue.addAll(
        gates.stream()
            .map(gate -> new PieceGenerator(SectionType.CONNECTOR, this, gate, null))
            .toList()
    );
  }

  public boolean isValidPlacement(DungeonPiece piece) {
    var bb = piece.getBounds();
    var intersecting = level.getIntersecting(bb);

    if (intersecting.isEmpty()) {
      return true;
    }

    for (var p : intersecting) {
      var intersection = p.getBounds().intersection(bb)
          .size();

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

        if (parent == null) {
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
        genQueue.addFirst(switchGeneratorType(section, section.getOrigin(), section.getParent()));
      }

      default ->
          throw new IllegalArgumentException("Invalid return code: " + result.getResultCode());
    }
  }

  PieceGenerator switchGeneratorType(PieceGenerator existing, DungeonGate origin,
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
      public Result onGate(DungeonGate gate) {
        if (gate.hasChildren()
            || config.getRandom().nextFloat() > config.getDecorateGateRate()
        ) {
          return Result.CONTINUE;
        }

        DungeonRoom parent = (DungeonRoom) gate.getParent();
        Holder<GateType> gateType = Pieces.getClosed(config.getRandom());

        DungeonGate g = gateType.getValue().create();
        NodeAlign.align(g, gate.getParentExit(),
            gateType.getValue().getGates().get(GATE_INDEX_ENTRANCE));

        if (!isValidPlacement(g)) {
          return Result.CONTINUE;
        }

        parent.removeChild(gate);
        parent.addChild(g);

        return Result.CONTINUE;
      }

      @Override
      public Result onRoom(DungeonRoom room) {
        return Result.CONTINUE;
      }
    };

    root.visit(visitor);
  }

  private boolean removeDeadEnds(DungeonRoom root) {
    Set<DungeonRoom> endRooms = new ObjectOpenHashSet<>();

    PieceVisitor visitor = new PieceVisitor() {
      @Override
      public Result onGate(DungeonGate gate) {
        if (!gate.hasChildren()) {
          endRooms.add((DungeonRoom) gate.getParent());
        }

        return Result.CONTINUE;
      }

      @Override
      public Result onRoom(DungeonRoom room) {
        return Result.CONTINUE;
      }
    };

    root.visit(visitor);

    endRooms.removeIf(room -> !room.getType().hasFlags(Pieces.FLAG_CONNECTOR));
    boolean result = false;

    for (var d : endRooms) {
      DungeonGate parent = (DungeonGate) d.getParent();

      if (!d.hasChildren() || canRemoveNode(d)) {
        parent.removeChild(d);
        parent.setOpen(false);
        result = true;
      }
    }

    return result;
  }

  private boolean canRemoveNode(DungeonRoom piece) {
    AtomicBoolean atomicBoolean = new AtomicBoolean(true);

    PieceVisitor visitor = new PieceVisitor() {
      @Override
      public Result onGate(DungeonGate gate) {
        if (gate.hasChildren()) {
          atomicBoolean.set(false);
          return Result.STOP;
        }

        return Result.CONTINUE;
      }

      @Override
      public Result onRoom(DungeonRoom room) {
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
    RoomType bossRoom = DungeonManager.getInstance().getRoomTypes()
        .getRandom(
            config.getRandom(),
            roomTypeHolder -> roomTypeHolder.getValue().hasFlags(
                Pieces.FLAG_BOSS_ROOM)
        )
        .map(Holder::getValue)
        .orElse(null);

    if (bossRoom == null) {
      LOGGER.warn("Found no boss room to append to dungeon!");
      return;
    }

    for (DungeonGate g : result.endGates) {
      DungeonRoom room = bossRoom.create();

      NodeAlign.align(
          room,
          g.getTargetGate(),
          bossRoom.getGates().get(GATE_INDEX_ENTRANCE)
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

  record LevelGenResult(DungeonLevel level,
                        int nonConnectorRooms,
                        IntSummaryStatistics endDepthStats,
                        List<DungeonGate> endGates,
                        int totalRoomCount,
                        int closedEndConnectors
  ) {

  }

  static class ValidationVisitor implements PieceVisitor {

    private final List<DungeonGate> endGates = new ObjectArrayList<>();

    private int nonConnectorRooms;
    private int totalRoomCount;
    private int closedEndConnectors;

    @Override
    public Result onGate(DungeonGate gate) {
      // Close gates that have no children
      if (gate.hasChildren()) {
        return Result.CONTINUE;
      }

      endGates.add(gate);

      if (gate.isOpen()) {
        gate.setOpen(false);
      }

      if (gate.getParent() instanceof DungeonRoom room
          && room.getType().hasFlags(Pieces.FLAG_CONNECTOR)
      ) {
        closedEndConnectors++;
      }

      return Result.CONTINUE;
    }

    @Override
    public Result onRoom(DungeonRoom room) {
      if (!room.getType().hasFlags(Pieces.FLAG_CONNECTOR)) {
        ++nonConnectorRooms;
      }

      ++totalRoomCount;
      return Result.CONTINUE;
    }
  }
}
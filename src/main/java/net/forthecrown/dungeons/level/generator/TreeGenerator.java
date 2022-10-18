package net.forthecrown.dungeons.level.generator;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.forthecrown.core.Crown;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.dungeons.level.*;
import net.forthecrown.dungeons.level.gate.DungeonGate;
import net.forthecrown.dungeons.level.gate.Gates;
import net.forthecrown.utils.math.Transform;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.forthecrown.dungeons.level.generator.SectionGenerator.GATE_INDEX_ENTRANCE;
import static net.forthecrown.dungeons.level.generator.StepResult.*;

@Getter
public class TreeGenerator {
    private static final Logger LOGGER = Crown.logger();

    public static final int MAX_OVERLAP = 2;

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    private final TreeGeneratorConfig config;
    private final Range<Integer> depthRange;

    private DungeonLevel level;
    private boolean finished = false;

    private final Deque<SectionGenerator> genQueue = new ArrayDeque<>();

    private final List<LevelGenResult> generationResults = new ObjectArrayList<>();

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

        generationResults.sort(this::compare);
        return generationResults.get(0).level();
    }

    private void createPotentialLevels() {
        int attempts = 0;

        while (attempts < config.getMaxGrowthAttempts()) {
            attempts++;

            reset();

            while (!isFinished()) {
                step();
            }

            // Recursively cut off all connector rooms that lead
            // to nothing
            while (removeDeadEnds(level.getRoot())) {}

            var result = compileResult();
            addBossRoom(result);
            addDecorateGates(result);

            generationResults.add(result);
        }
    }

    boolean isValidLevel(LevelGenResult result) {
        if (result.nonConnectorRooms < config.getRequiredRooms()) {
            return false;
        }

        if (!depthRange.contains(result.endDepthStats.getMax())) {
            return false;
        }

        return depthRange.contains(result.endDepthStats.getMin())
                || depthRange.contains((int) result.endDepthStats.getAverage());
    }

    static final int FIRST_BETTER = -1;
    static final int SECOND_BETTER = 1;
    static final int EQUAL = 0;

    int compare(LevelGenResult first, LevelGenResult second) {
        boolean firstValid = isValidLevel(first);
        boolean secondValid = isValidLevel(second);

        if (!secondValid && firstValid) {
            return FIRST_BETTER;
        }

        if (secondValid && !firstValid) {
            return SECOND_BETTER;
        }

        double nonConnectorRate1 = ((double) first.totalRoomCount) / first.nonConnectorRooms;
        double nonConnectorRate2 = ((double) second.totalRoomCount) / second.nonConnectorRooms;

        return Double.compare(nonConnectorRate2, nonConnectorRate1);
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
                walker.totalRoomCount
        );
    }

    public void reset() {
        finished = false;
        genQueue.clear();

        DungeonRoom root = Rooms.ROOT.getValue().create();
        root.apply(Transform.offset(config.getLocation()));

        level = new DungeonLevel();
        level.addPiece(root);

        var gates = SectionGenerator.createGates(root, root.getType().getGates());
        genQueue.addAll(
                gates.stream()
                        .map(gate -> new PathSection(this, gate, null))
                        .toList()
        );
    }

    public boolean isValidPlacement(DungeonPiece piece) {
        var bb = piece.getBounds();
        var intersecting = level.getIntersecting(bb);

        if (intersecting.isEmpty()) {
            return true;
        }

        for (var p: intersecting) {
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
        SectionGenerator<?> peeked = genQueue.poll();

        if (peeked == null) {
            finished = true;
            return;
        }

        var gate = peeked.origin;
        var result = peeked.generate();

        switch (result.getResultCode()) {
            case SUCCESS -> {
                result.getSections().forEach(genQueue::addLast);
                peeked.origin.addChild(result.getRoom());

                level.addPiece(result.getRoom());
            }

            case FAILED -> {
                SectionGenerator parent = peeked.sectionParent();

                if (parent == null) {
                    gate.setOpen(false);
                    return;
                }

                parent.failedTypes.add(gate.getParent().getType());
                parent.origin.clearChildren();

                genQueue.addFirst(parent);
            }

            case MAX_DEPTH -> {
                var parent = peeked.parent;

                if (peeked.sectionDepth < peeked.optimalDepth || parent instanceof PathSection) {
                    var root = peeked.sectionRoot();

                    root.origin.setOpen(true);
                    root.origin.clearChildren();

                    genQueue.addFirst(root);
                } else if (parent instanceof RoomSection) {
                    peeked.origin.setOpen(false);
                }
            }

            case MAX_SECTION_DEPTH -> {
                genQueue.addFirst(switchGeneratorType(peeked, peeked.origin, peeked.parent));
            }

            default -> throw new IllegalArgumentException("Invalid return code: " + result.getResultCode());
        }
    }

    SectionGenerator switchGeneratorType(SectionGenerator existing, DungeonGate origin, SectionGenerator parent) {
        if (existing instanceof RoomSection) {
            return new PathSection(TreeGenerator.this, origin, parent);
        } else {
            return new RoomSection(TreeGenerator.this, origin, parent);
        }
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
                var gateType = parent.getType().hasFlags(Rooms.FLAG_CONNECTOR)
                        ? Gates.COLLAPSED_GATE : Gates.DECORATE_GATE;

                DungeonGate g = gateType.getValue().create();
                NodeAlign.align(g, gate.getParentExit(), gateType.getValue().getGates().get(GATE_INDEX_ENTRANCE));

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

        endRooms.removeIf(room -> !room.getType().hasFlags(Rooms.FLAG_CONNECTOR));
        boolean result = false;

        for (var d: endRooms) {
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
        RoomType bossRoom = Rooms.REGISTRY
                .getRandom(
                        config.getRandom(),
                        roomTypeHolder -> roomTypeHolder.getValue().hasFlags(Rooms.FLAG_BOSS_ROOM)
                )
                .map(Holder::getValue)
                .orElse(null);

        if (bossRoom == null) {
            LOGGER.warn("Found no boss room to append to dungeon!");
            return;
        }

        for (DungeonGate g: result.endGates) {
            if (!g.isOpen()) {
                continue;
            }

            DungeonRoom room = bossRoom.create();

            NodeAlign.align(
                    room,
                    g.getTargetGate(),
                    bossRoom.getGates().get(GATE_INDEX_ENTRANCE)
            );

            if (isValidPlacement(room)) {
                g.setOpen(true);
                g.addChild(room);

                LOGGER.info("Appended boss room!");
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
                          int totalRoomCount
    ) {}

    static class ValidationVisitor implements PieceVisitor {
        private final List<DungeonGate> endGates = new ObjectArrayList<>();

        private int nonConnectorRooms;
        private int totalRoomCount;

        @Override
        public Result onGate(DungeonGate gate) {
            // Close gates that have no children
            if (!gate.hasChildren()) {
                endGates.add(gate);

                if (gate.isOpen()) {
                    gate.setOpen(false);
                }
            }

            return Result.CONTINUE;
        }

        @Override
        public Result onRoom(DungeonRoom room) {
            if (!room.getType().hasFlags(Rooms.FLAG_CONNECTOR)) {
                ++nonConnectorRooms;
            }

            ++totalRoomCount;
            return Result.CONTINUE;
        }
    }
}
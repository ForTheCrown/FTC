package net.forthecrown.dungeons.level.generator;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.DungeonRoom;
import net.forthecrown.dungeons.level.RoomType;
import net.forthecrown.dungeons.level.Rooms;
import net.forthecrown.dungeons.level.gate.AbsoluteGateData;
import net.forthecrown.dungeons.level.gate.DungeonGate;
import net.forthecrown.dungeons.level.gate.GateData;
import net.forthecrown.dungeons.level.gate.Gates;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.WeightedList;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static net.forthecrown.dungeons.level.generator.StepResult.*;

public abstract class SectionGenerator<T extends SectionGenerator<T>> {
    static final int
            GATE_INDEX_ENTRANCE = 0,
            GATE_INDEX_EXIT = 1,

            DEFAULT_WEIGHT = 10;

    protected final TreeGenerator gen;
    protected final TreeGeneratorConfig config;
    
    protected final int depth;
    protected final int sectionDepth;
    protected final int optimalDepth;
    protected final Range<Integer> depthRange;

    protected final DungeonGate origin;
    protected final @Nullable SectionGenerator parent;

    protected final List<RoomType> failedTypes = new ObjectArrayList<>();
    protected final WeightedList<RoomType> potentials = new WeightedList<>();

    protected int attempts = 0;

    public SectionGenerator(TreeGenerator gen, DungeonGate origin, @Nullable SectionGenerator parent) {
        this.gen = gen;
        this.config = gen.getConfig();

        this.origin = origin;
        this.parent = parent;

        if (parent == null) {
            this.depth = origin.getDepth() + 1;
        } else {
            this.depth = parent.depth + 1;
        }

        if (parent != null && parent.getClass() == this.getClass()) {
            depthRange = parent.depthRange;
            sectionDepth = parent.sectionDepth + 1;
            optimalDepth = parent.optimalDepth;
        } else {
            depthRange = createDepthRange(config);

            if (Objects.equals(depthRange.getMinimum() ,depthRange.getMaximum())) {
                optimalDepth = depthRange.getMinimum();
            } else {
                optimalDepth = config.getRandom().nextInt(
                        depthRange.getMinimum(),
                        depthRange.getMaximum() + 1
                );
            }

            sectionDepth = DungeonPiece.STARTING_DEPTH;
        }

        fillPotentials()
                .filter(pair -> {
                    if ((pair.value().getFlags() & (Rooms.FLAG_ROOT | Rooms.FLAG_BOSS_ROOM)) != 0) {
                        return false;
                    }

                    DungeonRoom parentRoom = (DungeonRoom) origin.getParent();

                    // 2 stair pieces cannot be right after each other
                    if (parentRoom.getType().hasFlags(Rooms.FLAG_STAIRS)
                            && pair.value().hasFlags(Rooms.FLAG_STAIRS)
                    ) {
                        return false;
                    }

                    return !origin.getParent().getType().equals(pair.second());
                })

                .forEach(pair -> potentials.add(pair.firstInt(), pair.second()));
    }

    public StepResult<T> generate() {
        if (sectionDepth > depthRange.getMaximum()) {
            return StepResult.failure(MAX_SECTION_DEPTH);
        }

        if (depth > config.getMaxDepth()) {
            return StepResult.failure(MAX_DEPTH);
        }

        // Make as many attempts as possible to find correct room
        while (attempts < config.getMaxGrowthAttempts()
                && !potentials.isEmpty()
        ) {
            attempts++;

            // This is a weighted iterator, it iterates through the list's entries
            // in a semi-random order, which is dictated by the weight of each entry
            // and the return values of the random we're giving it
            var it = potentials.iterator(config.getRandom());

            // Iterate through all room types
            while (it.hasNext()) {
                RoomType next = it.next();

                // Create gates and shuffle list order
                List<GateData> gates = next.getGates();
                Collections.shuffle(gates, config.getRandom());

                var gIt = gates.iterator();

                // Remove this room from the potentials list regardless
                // if it succeeds the valid placement check or not, this
                // is so if a child node is backtracked to this node,
                // then we don't end up placing this node again
                it.remove();

                // Iterate through all that room's gates to find
                // a gate that can be used as the entrance
                while (gIt.hasNext()) {
                    GateData gate = gIt.next();
                    DungeonRoom room = next.create();

                    NodeAlign.align(room, origin.getTargetGate(), gate);

                    // If invalid, move onto next type
                    if (!gen.isValidPlacement(room)) {
                        failedTypes.add(next);
                        continue;
                    }

                    // Remove gate, as it's now the entrance
                    gIt.remove();
                    
                    List<DungeonGate> exits = createGates(room, gates);

                    // Rooms should only have open gates, and thus
                    // possibilities for further nodes, if we're
                    // currently below the minimum dungeon depth
                    if (this instanceof RoomSection) {
                        exits.forEach(gate1 -> gate1.setOpen(false));
                        var rand = config.getRandom();
                        
                        if ((depth <= config.getMinDepth()
                                || rand.nextFloat() <= config.getRoomOpenChance())
                                && !exits.isEmpty()
                        ) {
                            Crown.logger().info("exit size={}", exits.size());
                            Crown.logger().info("struct={}", next.getStructureName());

                            int keepOpen = exits.size() == 1 ? 1 : config.getRandom().nextInt(1, exits.size());

                            // Keep random amount of gates open
                            Util.pickUniqueEntries(exits, config.getRandom(), keepOpen)
                                    .forEach(gate1 -> gate1.setOpen(true));
                        }
                    }

                    List<T> childSections = exits.stream()
                            .filter(DungeonGate::isOpen)
                            .map(this::createCopy)
                            .collect(ObjectArrayList.toList());

                    return StepResult.success(childSections, room);
                }
            }
        }

        return StepResult.failure(FAILED);
    }

    protected abstract Stream<IntObjectPair<RoomType>> fillPotentials();

    protected abstract Range<Integer> createDepthRange(TreeGeneratorConfig config);

    protected abstract T createCopy(DungeonGate gate);

    static List<DungeonGate> createGates(DungeonRoom piece, List<GateData> exits) {
        return exits.stream()
                .map(data1 -> data1.toAbsolute(piece))
                .map(exit -> {
                    DungeonGate gate = Gates.DEFAULT_GATE.getValue().create();
                    List<GateData> gates = gate.getType().getGates();

                    Validate.isTrue(!gates.isEmpty(), "%s has no gates, cannot use!", gate.getType().getStructureName());

                    AbsoluteGateData entrance = NodeAlign.align(gate, exit, gates.get(GATE_INDEX_ENTRANCE));
                    piece.addChild(gate);

                    // No exit
                    if (gates.size() == 1) {
                        gate.setOpen(false);
                    } else {
                        gate.setTargetGate(gates.get(GATE_INDEX_EXIT).toAbsolute(gate));
                    }

                    return gate;
                })
                .collect(ObjectArrayList.toList());
    }

    public T sectionParent() {
        // If parent exists and is not a different type of
        // section, return parent casted
        if (parent != null
                && parent.getClass() == this.getClass()
        ) {
            return (T) parent;
        }

        return null;
    }

    public T sectionRoot() {
        T root = sectionParent();

        while (root != null && root.sectionParent() != null) {
            root = root.sectionParent();
        }

        return root;
    }
}
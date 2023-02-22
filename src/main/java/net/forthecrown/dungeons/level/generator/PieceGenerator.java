package net.forthecrown.dungeons.level.generator;

import static net.forthecrown.dungeons.level.generator.StepResult.FAILED;
import static net.forthecrown.dungeons.level.generator.StepResult.MAX_DEPTH;
import static net.forthecrown.dungeons.level.generator.StepResult.MAX_SECTION_DEPTH;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.gate.GateData;
import net.forthecrown.dungeons.level.gate.GatePiece;
import net.forthecrown.dungeons.level.gate.Gates;
import net.forthecrown.dungeons.level.room.RoomFlag;
import net.forthecrown.dungeons.level.room.RoomPiece;
import net.forthecrown.dungeons.level.room.RoomType;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.WeightedList;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Getter
public class PieceGenerator {

  static final int
      GATE_INDEX_ENTRANCE = 0,
      GATE_INDEX_EXIT = 1,

      DEFAULT_WEIGHT = 10;

  private static final Logger LOGGER = Loggers.getLogger();

  private final TreeGenerator gen;
  private final TreeGeneratorConfig config;

  private final int depth;
  private final int sectionDepth;

  private final SectionType type;
  private final SectionData data;

  private final GatePiece origin;
  private final @Nullable PieceGenerator parent;

  private final List<RoomType> failedTypes = new ObjectArrayList<>();
  private final WeightedList<RoomType> potentials = new WeightedList<>();

  public PieceGenerator(SectionType type,
                        TreeGenerator gen,
                        GatePiece origin,
                        @Nullable PieceGenerator parent
  ) {
    this.type = type;
    this.gen = gen;
    this.config = gen.getConfig();
    this.origin = origin;
    this.parent = parent;
    this.depth = origin.getDepth() + 1;

    if (parent != null && parent.type == type) {
      this.data = parent.data;
      this.sectionDepth = parent.sectionDepth + 1;
    } else {
      Range<Integer> depthRange = type.createDepth(config);
      int maxRooms = depthRange.getMaximum() * 2;

      int optimal = depthRange.getMinimum();

      if (!Objects.equals(depthRange.getMinimum(), depthRange.getMaximum())) {
        optimal = config.getRandom().nextInt(
            depthRange.getMinimum(),
            depthRange.getMaximum() + 1
        );
      }

      this.data = new SectionData(maxRooms, optimal, depthRange);

      sectionDepth = DungeonPiece.STARTING_DEPTH;
    }

    type.fillPotentials(this).filter(pair -> {
      var flags = pair.value().getFlags();

      if (flags.contains(RoomFlag.ROOT)
          || flags.contains(RoomFlag.BOSS_ROOM)
      ) {
        return false;
      }

      // Ensure connectors have matching opening sizes
      GateData.Opening opening = getOrigin()
          .getTargetGate()
          .opening();

      var gates = pair.second().getGates();
      int matchingOpenings = 0;

      for (var g: gates) {
        if (g.opening().equals(opening)) {
          matchingOpenings++;
        }
      }

      return !data.successful.contains(pair.second())
          && matchingOpenings > 0;
    })
        .forEach(pair -> potentials.add(pair.firstInt(), pair.second()));

    if (potentials.isEmpty()) {
      LOGGER.error("NO POTENTIALS", new Throwable());
    }
  }

  private StepResult sectionDepthFailure() {
    return StepResult.failure(MAX_SECTION_DEPTH);
  }

  public StepResult generate() {
    if (sectionDepth > data.depthRange.getMaximum()) {
      return sectionDepthFailure();
    }

    if (sectionDepth > data.getOptimalDepth()
        && config.getRandom().nextInt(4) == 0
    ) {
      return sectionDepthFailure();
    }

    if ((data.roomCount + 1) > data.maxRooms) {
      return sectionDepthFailure();
    }

    if (depth > config.getMaxDepth()) {
      return StepResult.failure(MAX_DEPTH);
    }

    // Make as many attempts as possible to find correct room
    while (!potentials.isEmpty()) {
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
          GateData entrance = gIt.next();
          RoomPiece room = next.create();

          // Stair gates cannot connect to each other
          if (entrance.stairs() && origin.getParentExit().stairs()) {
            continue;
          }

          NodeAlign.align(room, origin.getTargetGate(), entrance);

          // If invalid, move onto next type
          if (!gen.isValidPlacement(room)) {
            failedTypes.add(next);
            continue;
          }

          // Remove gate, as it's now the entrance
          gIt.remove();

          List<GatePiece> exits = Gates.createExitGates(
              room, gates, config.getRandom()
          );

          // Close random gates if we have more than
          // max amount of gates
          if (exits.size() > type.getMaxExits(config)) {
            Util.pickUniqueEntries(exits, gen.getConfig().getRandom(), type.getMaxExits(config))
                .forEach(gate1 -> gate1.setOpen(false));
          }

          // Rooms should only have open gates, and thus
          // possibilities for further nodes, if we're
          // currently below the minimum dungeon depth,
          // or a random value is below the room open chance
          if (this.type == SectionType.ROOM) {
            exits.forEach(gate1 -> gate1.setOpen(false));
            var rand = config.getRandom();

            // If room is being placed below the min dungeon depth
            // or if a random allows us to, add extra path sections
            // to the created room's gates
            if ((depth <= config.getMinDepth()
                || rand.nextFloat() <= config.getRoomOpenChance())
                && !exits.isEmpty()
            ) {
              int keepOpen = exits.size() == 1 ? 1 : rand.nextInt(
                  1,
                  Math.min(exits.size(), type.getMaxExits(config))
              );

              // Keep random amount of gates open
              Util.pickUniqueEntries(exits, rand, keepOpen)
                  .forEach(gate1 -> gate1.setOpen(true));
            }
          }

          List<PieceGenerator> childSections = exits.stream()
              .filter(GatePiece::isOpen)
              .map(this::newGeneratorForGate)
              .collect(ObjectArrayList.toList());

          return StepResult.success(childSections, room);
        }
      }
    }

    return StepResult.failure(FAILED);
  }

  private PieceGenerator newGeneratorForGate(GatePiece gate) {
    return new PieceGenerator(type, gen, gate, this);
  }

  public @Nullable PieceGenerator sectionParent() {
    if (parent != null && parent.type == type) {
      return parent;
    }

    return null;
  }

  public @Nullable PieceGenerator sectionRoot() {
    PieceGenerator root = sectionParent();

    while (root != null && root.sectionParent() != null) {
      root = root.sectionParent();
    }

    return root;
  }

  public void onSuccess(RoomPiece success) {
    data.roomCount++;
    data.successful.add(success.getType());
    origin.addChild(success);
  }

  public void onChildFail(RoomType failed) {
    failedTypes.add(failed);
    data.roomCount--;
    data.successful.remove(failed);
    origin.clearChildren();
  }

  @Getter
  @RequiredArgsConstructor
  public static class SectionData {

    private final int maxRooms;
    private final int optimalDepth;
    private final Range<Integer> depthRange;

    public int roomCount;
    private final List<RoomType> successful = new ObjectArrayList<>();
  }
}
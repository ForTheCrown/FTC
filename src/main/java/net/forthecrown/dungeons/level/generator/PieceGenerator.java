package net.forthecrown.dungeons.level.generator;

import static net.forthecrown.dungeons.level.generator.StepResult.FAILED;
import static net.forthecrown.dungeons.level.generator.StepResult.MAX_DEPTH;
import static net.forthecrown.dungeons.level.generator.StepResult.MAX_SECTION_DEPTH;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.DungeonRoom;
import net.forthecrown.dungeons.level.Pieces;
import net.forthecrown.dungeons.level.RoomType;
import net.forthecrown.dungeons.level.gate.DungeonGate;
import net.forthecrown.dungeons.level.gate.GateData;
import net.forthecrown.dungeons.level.gate.GateType;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.WeightedList;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

@Getter
public class PieceGenerator {

  static final int
      GATE_INDEX_ENTRANCE = 0,
      GATE_INDEX_EXIT = 1,

  DEFAULT_WEIGHT = 10;

  private final TreeGenerator gen;
  private final TreeGeneratorConfig config;

  private final int depth;
  private final int sectionDepth;

  private final SectionType type;
  private final SectionData data;

  private final DungeonGate origin;
  private final @Nullable PieceGenerator parent;

  private final List<RoomType> failedTypes = new ObjectArrayList<>();
  private final WeightedList<RoomType> potentials = new WeightedList<>();

  public PieceGenerator(SectionType type,
                        TreeGenerator gen,
                        DungeonGate origin,
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

    type.fillPotentials(this)
        .filter(pair -> {
          if ((pair.value().getFlags() & (Pieces.FLAG_ROOT | Pieces.FLAG_BOSS_ROOM)) != 0) {
            return false;
          }

          // Ensure connectors have matching opening sizes
          GateData.Opening opening = getOrigin()
              .getTargetGate()
              .opening();

          var gates = pair.second().getGates();
          int matchingOpenings = 0;

          for (var g : gates) {
            if (g.opening().equals(opening)) {
              matchingOpenings++;
            }
          }

          return !data.successful.contains(pair.second())
              && matchingOpenings > 0;
        })

        .forEach(pair -> potentials.add(pair.firstInt(), pair.second()));
  }

  public StepResult generate() {
    if (sectionDepth > data.depthRange.getMaximum()
        || (sectionDepth > data.getOptimalDepth() && config.getRandom().nextInt(4) == 0)
        || (data.roomCount + 1) > data.maxRooms
    ) {
      return StepResult.failure(MAX_SECTION_DEPTH);
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
          DungeonRoom room = next.create();

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

          List<DungeonGate> exits = createGates(room, gates);

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
              .filter(DungeonGate::isOpen)
              .map(this::newGeneratorForGate)
              .collect(ObjectArrayList.toList());

          return StepResult.success(childSections, room);
        }
      }
    }

    return StepResult.failure(FAILED);
  }

  private PieceGenerator newGeneratorForGate(DungeonGate gate) {
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

    while (root != null
        && root.sectionParent() != null
    ) {
      root = root.sectionParent();
    }

    return root;
  }

  public void onSuccess(DungeonRoom success) {
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

  static List<DungeonGate> createGates(DungeonRoom piece, List<GateData> exits) {
    return exits.stream()
        .map(data1 -> data1.toAbsolute(piece))
        .map(exit -> {
          // Find a gate which matches the exit's size
          Pair<Holder<GateType>, GateData>
              pair = Pieces.findGate(exit.opening(), false);

          Holder<GateType> gateType;
          GateData entrance;
          List<GateData> gates;

          // If exit not found, resort to using the default gate
          if (pair == null) {
            gateType = Pieces.getDefaultGate();
            gates = gateType.getValue().getGates();
            entrance = gates.get(GATE_INDEX_ENTRANCE);
          } else {
            gateType = pair.getFirst();
            entrance = pair.getSecond();
            gates = gateType.getValue().getGates();
          }

          DungeonGate gate = gateType.getValue().create();
          int entranceIndex = gates.indexOf(entrance);

          Validate.isTrue(
              !gates.isEmpty(),
              "%s has no gates, cannot use!",
              gate.getType().getStructureName()
          );

          NodeAlign.align(gate, exit, entrance);
          piece.addChild(gate);

          // No exit
          if (gates.size() == 1) {
            gate.setOpen(false);
          } else {
            int exitIndex = -1;

            // Find exit gate, Lazy way of finding the opposite
            // index of entranceIndex, since it probably only has 2
            // entrances
            while ((++exitIndex) == entranceIndex
                && exitIndex < gates.size()
            ) {

            }

            gate.setTargetGate(
                gates.get(exitIndex)
                    .toAbsolute(gate)
            );
          }

          return gate;
        })
        .collect(ObjectArrayList.toList());
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
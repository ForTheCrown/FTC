package net.forthecrown.dungeons.level.gate;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.dungeons.DungeonManager;
import net.forthecrown.dungeons.level.generator.NodeAlign;
import net.forthecrown.dungeons.level.room.RoomPiece;
import org.apache.logging.log4j.Logger;

public final class Gates {
  private Gates() {}

  private static final Logger LOGGER = Loggers.getLogger();

  public static List<GatePiece> createExitGates(RoomPiece parent,
                                                List<GateData> exitList,
                                                Random random
  ) {
    return exitList.stream()
        .map(gateData -> gateData.toAbsolute(parent))
        .map(data -> generateExit(data, random, parent))
        .collect(Collectors.toList());
  }

  private static GatePiece generateExit(AbsoluteGateData parentExit,
                                        Random random,
                                        RoomPiece parent
  ) {
    List<GateMatch> matches
        = findOpeningMatchingGates(parentExit.opening(), true);

    if (matches.isEmpty()) {
      LOGGER.warn("Couldn't find matching gates for opening: {}",
          parentExit.opening()
      );

      Holder<GateType> def = getDefaultGate();
      List<GateData> gates = def.getValue().getGates();

      if (gates.isEmpty()) {
        throw new IllegalStateException(
            "Default gate has no exits/entrances! (Forgot to mark them?)"
        );
      }

      IntList indices = createIndexList(gates.size());

      matches = ObjectLists.singleton(
          new GateMatch(def, gates, indices)
      );
    }

    GateMatch match = matches.get(random.nextInt(matches.size()));
    List<GateData> gates = match.gates();
    IntList indices = match.matchingIndices();

    int chosenGate = indices.getInt(random.nextInt(indices.size()));
    GateData chosenEntrance = gates.remove(chosenGate);

    GatePiece gate = match.type().getValue().create();

    NodeAlign.align(gate, parentExit, chosenEntrance);

    parent.addChild(gate);

    if (gates.isEmpty()) {
      gate.setOpen(false);
    } else {
      GateData exit = gates.remove(0);
      AbsoluteGateData absExit = exit.toAbsolute(gate);
      gate.setTargetGate(absExit);
    }

    if (!gates.isEmpty()) {
      LOGGER.error("Gate {} has more than 2 connection points",
          match.type().getKey()
      );
    }

    return gate;
  }

  public static List<GateMatch> findOpeningMatchingGates(
      GateData.Opening opening,
      boolean open
  ) {
    final int requiredGates = open ? 2 : 1;
    var registry = DungeonManager.getDungeons()
        .getGateTypes();

    List<GateMatch> results = new ObjectArrayList<>();

    for (var h : registry.entries()) {
      List<GateData> gates = new ObjectArrayList<>();
      gates.addAll(h.getValue().getGates());

      if (gates.size() < requiredGates) {
        continue;
      }

      IntList indices = new IntArrayList();

      var it = gates.listIterator();
      while (it.hasNext()) {
        int i = it.nextIndex();
        var n = it.next();

        if (n.opening().equals(opening)) {
          indices.add(i);
        }
      }

      if (indices.size() < requiredGates) {
        continue;
      }

      results.add(
          new GateMatch(h, gates, indices)
      );
    }

    return results;
  }

  private static IntList createIndexList(int size) {
    IntList list = new IntArrayList(size);

    for (int i = 0; i < size; i++) {
      list.add(i);
    }

    return list;
  }

  public static Holder<GateType> getClosed(Random random) {
    return DungeonManager.getDungeons().getGateTypes()
        .getRandom(random, holder -> !holder.getValue().isOpenable())
        .orElseThrow();
  }

  public static Holder<GateType> getDefaultGate() {
    return DungeonManager.getDungeons().getGateTypes()
        .getHolder("default")
        .orElseThrow(() -> new IllegalStateException("No default gate!"));
  }

  public record GateMatch(Holder<GateType> type,
                          List<GateData> gates,
                          IntList matchingIndices
  ) {

  }
}
package net.forthecrown.dungeons.level;

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
import net.forthecrown.dungeons.level.gate.AbsoluteGateData;
import net.forthecrown.dungeons.level.gate.GatePiece;
import net.forthecrown.dungeons.level.gate.GateData;
import net.forthecrown.dungeons.level.gate.GateType;
import net.forthecrown.dungeons.level.generator.NodeAlign;
import net.forthecrown.dungeons.level.room.RoomPiece;
import org.apache.logging.log4j.Logger;

public final class Gates {
  private Gates() {}

  private static final Logger LOGGER = Loggers.getLogger();

  public static List<GatePiece> createGates(RoomPiece room,
                                            List<GateData> dataList,
                                            Random random
  ) {
    return dataList.stream()
        .map(gateData -> gateData.toAbsolute(room))
        .map(data -> genGate(data, random, room))
        .collect(Collectors.toList());
  }

  private static GatePiece genGate(AbsoluteGateData data,
                                   Random random,
                                   RoomPiece parent
  ) {
    List<GateMatch> matches = matchOpening(data.opening(), false);

    if (matches.isEmpty()) {
      LOGGER.warn("Couldn't find matching gates for opening: {}",
          data.opening()
      );

      Holder<GateType> def = Pieces.getDefaultGate();
      List<GateData> gates = def.getValue().getGates();

      assert !gates.isEmpty()
          : "Default gate has no exits/entrances! (Forgot to mark them?)";

      IntList indices = createIndexList(gates.size());

      matches = ObjectLists.singleton(
          new GateMatch(def, gates, indices)
      );
    }

    GateMatch match = matches.get(random.nextInt(matches.size()));
    List<GateData> gates = match.gates;
    int chosenGate = match.matchingIndices
        .getInt(random.nextInt(match.matchingIndices.size()));

    GatePiece gate = match.type().getValue().create();
    NodeAlign.align(gate, data, gates.get(chosenGate));
    parent.addChild(gate);

    if (gates.size() == 1) {
      gate.setOpen(false);
    } else {
      int exit = -1;

      // Find exit gate by incrementing exit index from -1
      while ((++exit) != chosenGate && exit < gates.size()) {

      }

      gate.setTargetGate(gates.get(exit).toAbsolute(gate));
    }

    return gate;
  }

  public static List<GateMatch> matchOpening(GateData.Opening opening,
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
    IntList list = new IntArrayList();

    for (int i = 0; i < size; i++) {
      list.add(i);
    }

    return list;
  }

  public record GateMatch(Holder<GateType> type,
                          List<GateData> gates,
                          IntList matchingIndices
  ) {

  }
}
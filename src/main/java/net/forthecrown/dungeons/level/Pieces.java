package net.forthecrown.dungeons.level;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.Random;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.core.registry.RegistryKey;
import net.forthecrown.dungeons.DungeonManager;
import net.forthecrown.dungeons.level.gate.GateData;
import net.forthecrown.dungeons.level.gate.GateType;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class Pieces {

  public static final int
      FLAG_CONNECTOR = 0x1,
      FLAG_BOSS_ROOM = 0x2,
      FLAG_MOB_ROOM = 0x8,
      FLAG_ROOT = 0x20;

  public static final ImmutableMap<String, Integer> FLAGS_BY_NAME = ImmutableMap.<String, Integer>builder()
      .put("connector", FLAG_CONNECTOR)
      .put("boss_room", FLAG_BOSS_ROOM)
      .put("mob_room", FLAG_MOB_ROOM)
      .put("root", FLAG_ROOT)
      .build();

  private Pieces() {
  }

  public static PieceType load(Tag t) {
    RegistryKey key = RegistryKey.load(t);

    if (key == null) {
      return null;
    }

    Registry<PieceType> registry = DungeonManager.getInstance()
        .getTypeRegistries()
        .orNull(key.getRegistry());

    if (registry == null) {
      return null;
    }

    return registry.orNull(key.getValue());
  }

  public static StringTag save(PieceType type) {
    var registry = DungeonManager.getInstance()
        .getTypeRegistries();

    for (var r : registry.entries()) {
      var holder = r.getValue()
          .getHolderByValue(type)
          .map(holder1 -> RegistryKey.of(r.getKey(), holder1.getKey()).save());

      if (holder.isEmpty()) {
        continue;
      }

      return holder.get();
    }

    return null;
  }

  public static Pair<Holder<GateType>, GateData> findGate(GateData.Opening opening,
                                                          boolean open
  ) {
    final int requiredGates = open ? 2 : 1;
    var registry = DungeonManager.getInstance()
        .getGateTypes();

    for (var h : registry.entries()) {
      var gates = h.getValue().getGates();

      if (gates.size() < requiredGates) {
        continue;
      }

      for (var g : gates) {
        if (g.opening().equals(opening)) {
          return Pair.of(h, g);
        }
      }
    }

    return null;
  }

  public static Holder<RoomType> getRoot() {
    var reg = DungeonManager.getInstance().getRoomTypes();

    for (var e : reg.entries()) {
      if (e.getValue().hasFlags(FLAG_ROOT)) {
        return e;
      }
    }

    throw new IllegalStateException("No root room set");
  }

  public static Holder<GateType> getClosed(Random random) {
    return DungeonManager.getInstance().getGateTypes()
        .getRandom(random, holder -> {
          return !holder.getValue()
              .isOpenable();
        })
        .orElseThrow();
  }

  public static Holder<GateType> getDefaultGate() {
    return DungeonManager.getInstance().getGateTypes()
        .getHolder("default")
        .orElseThrow();
  }
}
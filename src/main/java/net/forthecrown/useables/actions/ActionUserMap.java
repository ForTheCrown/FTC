package net.forthecrown.useables.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import java.util.EnumMap;
import java.util.UUID;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.IntTag;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.Usable;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageType;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.UUID2IntMap;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ActionUserMap extends UsageAction {

  private final Action action;
  private final UUID2IntMap map;

  private final int amount;

  public ActionUserMap(UsageType<ActionUserMap> type, int amount) {
    super(type);

    this.amount = amount;
    Pair<Type, Action> pair = Type.findByType(type);

    this.action = pair.right();
    this.map = pair.left().getMap();
  }

  @Override
  public void onUse(Player player, Usable holder) {
    action.apply(map, player.getUniqueId(), amount);
  }

  @Override
  public @Nullable Component displayInfo() {
    return Component.text(amount);
  }

  @Override
  public @Nullable BinaryTag save() {
    return BinaryTags.intTag(amount);
  }

  // --- TYPE CONSTRUCTORS ---

  @UsableConstructor(ConstructType.PARSE)
  public static ActionUserMap parse(UsageType<ActionUserMap> type,
                                    StringReader reader,
                                    CommandSource source
  ) throws CommandSyntaxException {
    return new ActionUserMap(type, reader.readInt());
  }

  @UsableConstructor(ConstructType.TAG)
  public static ActionUserMap load(UsageType<ActionUserMap> type, BinaryTag tag) {
    return new ActionUserMap(type, ((IntTag) tag).intValue());
  }

  // --- SUB CLASSES ---

  enum Type {
    BAL {
      @Override
      UUID2IntMap getMap() {
        return UserManager.get().getBalances();
      }
    },

    VOTES {
      @Override
      UUID2IntMap getMap() {
        return UserManager.get().getVotes();
      }
    },

    GEMS {
      @Override
      UUID2IntMap getMap() {
        return UserManager.get().getGems();
      }
    };

    abstract UUID2IntMap getMap();

    final EnumMap<Action, UsageType<ActionUserMap>> typesByAction
        = Util.make(new EnumMap<>(Action.class), map -> {
          for (var t : Action.values()) {
            map.put(t, UsageType.of(ActionUserMap.class));
          }
        });

    static void registerAll() {
      var registry = UsageActions.REGISTRY;

      for (var t : values()) {
        for (var e : t.typesByAction.entrySet()) {
          UsageType<ActionUserMap> type = e.getValue();
          Action action = e.getKey();

          String key = action.name().toLowerCase() + "_" + t.name().toLowerCase();
          registry.register(key, type);
        }
      }
    }

    static Pair<Type, Action> findByType(UsageType<ActionUserMap> type) {
      for (var t : values()) {
        for (var a : t.typesByAction.entrySet()) {
          if (a.getValue().equals(type)) {
            return Pair.of(t, a.getKey());
          }
        }
      }

      throw new IllegalStateException();
    }
  }

  enum Action {
    ADD {
      @Override
      void apply(UUID2IntMap map, UUID uuid, int amount) {
        map.add(uuid, amount);
      }
    },

    SET {
      @Override
      void apply(UUID2IntMap map, UUID uuid, int amount) {
        map.set(uuid, amount);
      }
    },

    REMOVE {
      @Override
      void apply(UUID2IntMap map, UUID uuid, int amount) {
        map.remove(uuid, amount);
      }
    };

    abstract void apply(UUID2IntMap map, UUID uuid, int amount);
  }
}
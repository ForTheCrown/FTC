package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.*;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.UserScoreMap;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.UUID;

public class ActionUserMap extends UsageAction {
    private final Action action;
    private final UserScoreMap map;

    private final int amount;

    public ActionUserMap(UsageType<ActionUserMap> type, int amount) {
        super(type);

        this.amount = amount;
        Pair<Type, Action> pair = Type.findByType(type);

        this.action = pair.right();
        this.map = pair.left().getMap();
    }

    @Override
    public void onUse(Player player, ActionHolder holder) {
        action.apply(map, player.getUniqueId(), amount);
    }

    @Override
    public @Nullable Component displayInfo() {
        return Component.text(amount);
    }

    @Override
    public @Nullable Tag save() {
        return IntTag.valueOf(amount);
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor(ConstructType.PARSE)
    public static ActionUserMap parse(UsageType<ActionUserMap> type, StringReader reader, CommandSource source)
            throws CommandSyntaxException
    {
        return new ActionUserMap(type, reader.readInt());
    }

    @UsableConstructor(ConstructType.JSON)
    public static ActionUserMap fromJson(UsageType<ActionUserMap> type, JsonElement element) {
        return new ActionUserMap(type, element.getAsInt());
    }

    @UsableConstructor(ConstructType.TAG)
    public static ActionUserMap load(UsageType<ActionUserMap> type, Tag tag) {
        return new ActionUserMap(type, ((IntTag) tag).getAsInt());
    }

    // --- SUB CLASSES ---

    enum Type {
        BAL {
            @Override
            UserScoreMap getMap() {
                return UserManager.get().getBalances();
            }
        },

        VOTES {
            @Override
            UserScoreMap getMap() {
                return UserManager.get().getVotes();
            }
        },

        GEMS {
            @Override
            UserScoreMap getMap() {
                return UserManager.get().getGems();
            }
        };

        abstract UserScoreMap getMap();

        EnumMap<Action, UsageType<ActionUserMap>> typesByAction = Util.make(new EnumMap<>(Action.class), map -> {
            for (var t: Action.values()) {
                map.put(t, UsageType.of(ActionUserMap.class));
            }
        });

        static void registerAll() {
            var registry = Registries.USAGE_ACTIONS;

            for (var t: values()) {
                for (var e: t.typesByAction.entrySet()) {
                    UsageType type = e.getValue();
                    Action action = e.getKey();

                    String key = action.name().toLowerCase() + "_" + t.name().toLowerCase();
                    registry.register(key, type);
                }
            }
        }

        static Pair<Type, Action> findByType(UsageType<ActionUserMap> type) {
            for (var t: values()) {
                for (var a: t.typesByAction.entrySet()) {
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
            void apply(UserScoreMap map, UUID uuid, int amount) {
                map.add(uuid, amount);
            }
        },

        SET {
            @Override
            void apply(UserScoreMap map, UUID uuid, int amount) {
                map.set(uuid, amount);
            }
        },

        REMOVE {
            @Override
            void apply(UserScoreMap map, UUID uuid, int amount) {
                map.remove(uuid, amount);
            }
        };

        abstract void apply(UserScoreMap map, UUID uuid, int amount);
    }
}
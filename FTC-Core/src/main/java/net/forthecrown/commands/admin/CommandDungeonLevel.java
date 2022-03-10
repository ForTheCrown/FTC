package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Keys;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.ComponentTagVisitor;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.dungeons.level.DungeonLevelImpl;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.BaseSpawner;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;

import java.util.concurrent.CompletableFuture;

public class CommandDungeonLevel extends FtcCommand {

    public CommandDungeonLevel() {
        super("DungeonLevel");

        setPermission(Permissions.ADMIN);
        setAliases("dlevel");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /DungeonLevel
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("create")
                        .then(argument("key", Keys.argumentType())
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    Player wePlayer = BukkitAdapter.adapt(user.getPlayer());

                                    Key key = c.getArgument("key", NamespacedKey.class);
                                    if(Registries.DUNGEON_LEVELS.contains(key)) {
                                        throw FtcExceptionProvider.create("This level already exists");
                                    }

                                    Region selection = FtcUtils.getSelectionSafe(wePlayer);
                                    Bounds3i bounds = Bounds3i.of(selection);

                                    DungeonLevel level = new DungeonLevelImpl(key);
                                    level.setBounds(bounds);
                                    level.setWorld(user.getWorld());

                                    Registries.DUNGEON_LEVELS.register(key, level);

                                    c.getSource().sendAdmin("Level created");
                                    return 0;
                                })
                        )
                )

                .then(argument("level", RegistryArguments.dungeonLevel())
                        .then(literal("scan_spawners")
                                .executes(c -> {
                                    DungeonLevel level = get(c);

                                    CrownUser user = getUserSender(c);
                                    Player wePlayer = BukkitAdapter.adapt(user.getPlayer());

                                    Region selection = FtcUtils.getSelectionSafe(wePlayer);

                                    WorldBounds3i bounds = WorldBounds3i.of(user.getWorld(), selection);
                                    int added = 0;

                                    for (Block b: bounds) {
                                        if(b.getType() != Material.SPAWNER) continue;
                                        level.addSpawner(b);

                                        added++;
                                    }

                                    c.getSource().sendAdmin(
                                            ChatUtils.format("Added {} spawners to {} level", added, level.key())
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("place_spawners")
                                .executes(c -> {
                                    DungeonLevel level = get(c);
                                    level.placeSpawners();

                                    c.getSource().sendAdmin(
                                            ChatUtils.format("Placed spawners of level {}", level.key())
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("redefine")
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    Player wePlayer = BukkitAdapter.adapt(user.getPlayer());

                                    DungeonLevel level = get(c);
                                    Bounds3i newBounds = Bounds3i.of(FtcUtils.getSelectionSafe(wePlayer));
                                    Bounds3i oldBounds = level.getBounds();

                                    level.setBounds(newBounds);

                                    c.getSource().sendAdmin(
                                            ChatUtils.format("Set bounds of {} from {} to {}",
                                                    level.key(), oldBounds, newBounds
                                            )
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("spawners")
                                .then(literal("add")
                                        .then(argument("pos", PositionArgument.blockPos())
                                                .then(argument("data", NbtTagArgument.nbtTag())
                                                        .executes(c -> {
                                                            DungeonLevel level = get(c);
                                                            Vector3i pos = getPos(c);
                                                            Tag parsedTag = NbtTagArgument.getNbtTag(c, "data");

                                                            if(!(parsedTag instanceof CompoundTag tag)) {
                                                                throw FtcExceptionProvider.create("Tag must be a compound");
                                                            }

                                                            level.addSpawner(pos, tag);

                                                            c.getSource().sendAdmin(
                                                                    ChatUtils.format("Added spawner at {} to {} with data {}",
                                                                            pos, level.key(), tag
                                                                    )
                                                            );
                                                            return 0;
                                                        })
                                                )
                                        )
                                )

                                .then(editArg())
                        )
                );
    }

    RequiredArgumentBuilder<CommandSource, ?> editArg() {
        RequiredArgumentBuilder<CommandSource, ?> result = argument("pos", PositionArgument.blockPos())
                .then(literal("place")
                        .executes(c -> {
                            DungeonLevel level = get(c);
                            Vector3i pos = getPos(c);
                            BaseSpawner spawner = getSpawner(level, pos);

                            DungeonLevelImpl.place(spawner, VanillaAccess.getLevel(level.getWorld()), pos.toNms());

                            c.getSource().sendAdmin(
                                    ChatUtils.format("Placed spawner at {}", pos)
                            );
                            return 0;
                        })
                );

        for (SpawnerAccessor a: SpawnerAccessor.ACCESSORS) {
            result.then(accessor(a));
        }

        return result;
    }

    DungeonLevel get(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return c.getArgument("level", DungeonLevel.class);
    }

    <T> LiteralArgumentBuilder<CommandSource> accessor(SpawnerAccessor<T> acc) {
        return literal(acc.getName())
                .executes(c -> {
                    DungeonLevel level = get(c);
                    Vector3i pos = getPos(c);
                    BaseSpawner spawner = getSpawner(level, pos);

                    Component display = acc.display(spawner);

                    c.getSource().sendMessage(
                            Component.text(acc.getName() + " of spawner at " + pos + ": ")
                                    .append(display)
                    );
                    return 0;
                })

                .then(argument("arg", acc.getArgument())
                        .suggests((c, builder) -> {
                            DungeonLevel level = get(c);
                            Vector3i pos = getPos(c);
                            BaseSpawner spawner = getSpawner(level, pos);

                            return acc.suggest(c, builder, spawner);
                        })

                        .executes(c -> {
                            DungeonLevel level = get(c);
                            Vector3i pos = getPos(c);
                            BaseSpawner spawner = getSpawner(level, pos);
                            T val = acc.get(c, "arg");

                            acc.set(pos, level, spawner, val);
                            Component display = acc.display(spawner);

                            c.getSource().sendAdmin(
                                    Component.text("Set " + acc.getName() + " of spawner at " + pos + " to ")
                                            .append(display)
                            );
                            return 0;
                        })
                );
    }

    private Vector3i getPos(CommandContext<CommandSource> c) {
        return Vector3i.of(PositionArgument.getLocation(c, "pos"));
    }

    private BaseSpawner getSpawner(DungeonLevel level, Vector3i pos) throws RoyalCommandException {
        BaseSpawner spawner = level.getSpawner(pos);

        if(spawner == null) {
            throw FtcExceptionProvider.create("No spawner at position: " + pos);
        }

        return spawner;
    }

    private interface SpawnerAccessor<T> {
        IntAccessor MIN_SPAWN_DELAY = new IntAccessor() {
            @Override
            public int getVal(BaseSpawner spawner) {
                return spawner.minSpawnDelay;
            }

            @Override
            public void set(BaseSpawner spawner, Integer val) {
                spawner.minSpawnDelay = val;
            }

            @Override
            public String getName() {
                return "minSpawnDelay";
            }
        };
        IntAccessor MAX_SPAWN_DELAY = new IntAccessor() {
            @Override
            public int getVal(BaseSpawner spawner) {
                return spawner.maxSpawnDelay;
            }

            @Override
            public void set(BaseSpawner spawner, Integer val) {
                spawner.maxSpawnDelay = val;
            }

            @Override
            public String getName() {
                return "maxSpawnDelay";
            }
        };
        IntAccessor SPAWN_COUNT = new IntAccessor() {
            @Override
            public int getVal(BaseSpawner spawner) {
                return spawner.spawnCount;
            }

            @Override
            public void set(BaseSpawner spawner, Integer val) {
                spawner.spawnCount = val;
            }

            @Override
            public String getName() {
                return "spawnCount";
            }
        };
        IntAccessor MAX_NEARBY = new IntAccessor() {
            @Override
            public int getVal(BaseSpawner spawner) {
                return spawner.maxNearbyEntities;
            }

            @Override
            public void set(BaseSpawner spawner, Integer val) {
                spawner.maxNearbyEntities = val;
            }

            @Override
            public String getName() {
                return "maxNearbyEntities";
            }
        };
        IntAccessor PLAYER_RANGE = new IntAccessor() {
            @Override
            public int getVal(BaseSpawner spawner) {
                return spawner.requiredPlayerRange;
            }

            @Override
            public void set(BaseSpawner spawner, Integer val) {
                spawner.requiredPlayerRange = val;
            }

            @Override
            public String getName() {
                return "requiredPlayerRange";
            }
        };
        IntAccessor SPAWN_RANGE = new IntAccessor() {
            @Override
            public int getVal(BaseSpawner spawner) {
                return spawner.spawnRange;
            }

            @Override
            public void set(BaseSpawner spawner, Integer val) {
                spawner.spawnRange = val;
            }

            @Override
            public String getName() {
                return "spawnRange";
            }
        };

        SpawnerAccessor<CompoundTag> SPAWNER_DATA = new SpawnerAccessor<>() {
            @Override
            public Component display(BaseSpawner spawner) {
                CompoundTag tag = spawner.save(new CompoundTag());
                ComponentTagVisitor visitor = new ComponentTagVisitor(true);

                return visitor.visit(tag);
            }

            @Override
            public void set(Vector3i pos, DungeonLevel level, BaseSpawner spawner, CompoundTag val) {
                spawner.load(VanillaAccess.getLevel(level.getWorld()), pos.toNms(), val);
            }

            @Override
            public ArgumentType<?> getArgument() {
                return NbtTagArgument.nbtTag();
            }

            @Override
            public String getName() {
                return "data";
            }

            @Override
            public CompoundTag get(CommandContext<CommandSource> c, String argName) throws CommandSyntaxException {
                Tag t = NbtTagArgument.getNbtTag(c, argName);

                if(!(t instanceof CompoundTag tag)) {
                    throw FtcExceptionProvider.create("Must be compound tag");
                }

                return tag;
            }
        };

        SpawnerAccessor[] ACCESSORS = {
                MIN_SPAWN_DELAY, MAX_SPAWN_DELAY, SPAWN_COUNT,
                MAX_NEARBY, PLAYER_RANGE, SPAWN_RANGE, SPAWNER_DATA
        };

        Component display(BaseSpawner spawner);
        void set(Vector3i pos, DungeonLevel level, BaseSpawner spawner, T val);

        ArgumentType<?> getArgument();
        String getName();
        T get(CommandContext<CommandSource> c, String argName) throws CommandSyntaxException;

        default CompletableFuture<Suggestions> suggest(CommandContext<CommandSource> c, SuggestionsBuilder b, BaseSpawner spawner) {
            return Suggestions.empty();
        }
    }

    private interface IntAccessor extends SpawnerAccessor<Integer> {
        int getVal(BaseSpawner spawner);


        void set(BaseSpawner spawner, Integer val);

        @Override
        default void set(Vector3i pos, DungeonLevel level, BaseSpawner spawner, Integer val) {
            set(spawner, val);
        }

        @Override
        default Component display(BaseSpawner spawner) {
            return Component.text(getVal(spawner));
        }

        @Override
        default ArgumentType<?> getArgument() {
            return IntegerArgumentType.integer();
        }

        @Override
        default Integer get(CommandContext<CommandSource> c, String argName) throws CommandSyntaxException {
            return c.getArgument(argName, Integer.class);
        }
    }
}
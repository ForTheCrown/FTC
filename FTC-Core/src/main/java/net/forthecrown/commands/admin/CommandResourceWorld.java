package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.arguments.ChatArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Keys;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.ResourceWorld;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.text.Component;
import net.minecraft.world.level.Level;
import org.bukkit.NamespacedKey;
import org.mcteam.ancientgates.Gate;

import java.util.concurrent.CompletableFuture;

public class CommandResourceWorld extends FtcCommand {

    public CommandResourceWorld() {
        super("ResourceWorld");

        setAliases("rw");
        setPermission(Permissions.ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /ResourceWorld
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("regen")
                        .executes(c -> {
                            ResourceWorld rw = rw();

                            c.getSource().sendAdmin("Starting resource world regen");
                            rw.resetAndLoad();

                            return 0;
                        })
                )

                .then(accessor(RwAccessor.ENABLED))
                .then(accessor(RwAccessor.RESET_END_MESSAGE))
                .then(accessor(RwAccessor.RESET_START_MESSAGE))
                .then(accessor(RwAccessor.TO_HAZ_GATE))
                .then(accessor(RwAccessor.TO_RES_GATE))
                .then(accessor(RwAccessor.SIZE))
                .then(accessor(RwAccessor.SPAWN_STRUCTURE));
    }

    private <T> LiteralArgumentBuilder<CommandSource> accessor(RwAccessor<T> acc) {
        return literal(acc.getName())
                .executes(c -> {
                    ResourceWorld rw = rw();

                    c.getSource().sendMessage(
                            Component.text("[RW] ")
                                    .append(Component.text(acc.getName() + ": "))
                                    .append(acc.display(rw))
                    );
                    return 0;
                })

                .then(argument("val", acc.getArgumentType())
                        .executes(c -> {
                            T val = c.getArgument("val", acc.getTypeClass());
                            ResourceWorld rw = rw();

                            acc.set(val, rw);

                            c.getSource().sendAdmin(
                                    Component.text("[RW] Set " + acc.getName() + " to ")
                                            .append(acc.display(rw))
                            );
                            return 0;
                        })
                );
    }

    private ResourceWorld rw() {
        return null;
    }

    private interface RwAccessor<T> extends SuggestionProvider<CommandSource> {
        RwAccessor<Component> RESET_START_MESSAGE = new RwComponentAccessor() {
            @Override
            public Component display(ResourceWorld world) {
                return world.getResetStart();
            }

            @Override
            public void set(Component val, ResourceWorld world) {
                world.setResetStart(val);
            }

            @Override
            public String getName() {
                return "reset_start_msg";
            }
        };

        RwAccessor<Component> RESET_END_MESSAGE = new RwComponentAccessor() {
            @Override
            public Component display(ResourceWorld world) {
                return world.getResetEnd();
            }

            @Override
            public void set(Component val, ResourceWorld world) {
                world.setResetStart(val);
            }

            @Override
            public String getName() {
                return "reset_end_msg";
            }
        };

        RwAccessor<String> TO_RES_GATE = new RwGateAccessor() {
            @Override
            public Component display(ResourceWorld world) {
                return Component.text(world.getToResGate());
            }

            @Override
            public void set(String val, ResourceWorld world) throws CommandSyntaxException {
                validate(val);
                world.setToResGate(val);
            }

            @Override
            public String getName() {
                return "haz_to_res_gate";
            }
        };

        RwAccessor<String> TO_HAZ_GATE = new RwGateAccessor() {
            @Override
            public Component display(ResourceWorld world) {
                return Component.text(world.getToHazGate());
            }

            @Override
            public void set(String val, ResourceWorld world) throws CommandSyntaxException {
                validate(val);
                world.setToHazGate(val);
            }

            @Override
            public String getName() {
                return "res_to_haz_gate";
            }
        };

        RwAccessor<NamespacedKey> SPAWN_STRUCTURE = new RwAccessor<>() {
            @Override
            public Component display(ResourceWorld world) {
                return Component.text(world.getSpawnStructure().asString());
            }

            @Override
            public void set(NamespacedKey val, ResourceWorld world) throws CommandSyntaxException {
                if(!Registries.STRUCTURES.contains(val)) {
                    throw FtcExceptionProvider.create("Invalid structure ID '" + val + "'");
                }

                world.setSpawnStructure(val);
            }

            @Override
            public ArgumentType<NamespacedKey> getArgumentType() {
                return Keys.argumentType();
            }

            @Override
            public String getName() {
                return "spawn_structure";
            }

            @Override
            public Class<NamespacedKey> getTypeClass() {
                return NamespacedKey.class;
            }
        };

        RwAccessor<Boolean> ENABLED = new RwAccessor<Boolean>() {
            @Override
            public Component display(ResourceWorld world) {
                return Component.text(world.isAutoResetEnabled());
            }

            @Override
            public void set(Boolean val, ResourceWorld world) throws CommandSyntaxException {
                world.setAutoResetEnabled(val);
            }

            @Override
            public ArgumentType<Boolean> getArgumentType() {
                return BoolArgumentType.bool();
            }

            @Override
            public String getName() {
                return "enabled";
            }

            @Override
            public Class<Boolean> getTypeClass() {
                return Boolean.class;
            }
        };

        RwAccessor<Integer> SIZE = new RwAccessor<Integer>() {
            @Override
            public Component display(ResourceWorld world) {
                return Component.text(world.getSize());
            }

            @Override
            public void set(Integer val, ResourceWorld world) throws CommandSyntaxException {
                world.setSize(val);
            }

            @Override
            public ArgumentType<Integer> getArgumentType() {
                return IntegerArgumentType.integer(1, Level.MAX_LEVEL_SIZE - 6);
            }

            @Override
            public String getName() {
                return "world_size";
            }

            @Override
            public Class<Integer> getTypeClass() {
                return Integer.class;
            }
        };

        @Override
        default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return Suggestions.empty();
        }

        Component display(ResourceWorld world);
        void set(T val, ResourceWorld world) throws CommandSyntaxException;

        ArgumentType<T> getArgumentType();
        String getName();
        Class<T> getTypeClass();
    }

    private interface RwComponentAccessor extends RwAccessor<Component> {
        @Override
        default Class<Component> getTypeClass() {
            return Component.class;
        }

        @Override
        default ArgumentType<Component> getArgumentType() {
            return ChatArgument.chat();
        }
    }

    private interface RwGateAccessor extends RwAccessor<String> {
        @Override
        default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return CompletionProvider.suggestMatching(builder, Gate.getAllIDs());
        }

        @Override
        default ArgumentType<String> getArgumentType() {
            return StringArgumentType.word();
        }

        @Override
        default Class<String> getTypeClass() {
            return String.class;
        }

        default void validate(String val) throws CommandSyntaxException {
            Gate g = Gate.get(val);

            if(g == null) {
                throw FtcExceptionProvider.create("Invalid gate ID '" + val + "'");
            }
        }
    }
}
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
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Vars;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.config.ResourceWorldConfig;
import net.forthecrown.core.resource.ResourceWorld;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.text.Text;
import net.forthecrown.utils.Time;
import net.kyori.adventure.text.Component;
import net.minecraft.world.level.Level;
import org.bukkit.World;
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
                            ResourceWorld rw = ResourceWorld.get();

                            c.getSource().sendAdmin("Starting resource world regen");
                            rw.resetAndLoad();

                            return 0;
                        })
                )

                .then(literal("next_reset")
                        .executes(c -> {
                            long next = ResourceWorldConfig.lastReset + Vars.rwResetInterval;

                            if (Time.isPast(next)) {
                                c.getSource().sendMessage(
                                        "RW reset will happen during the next date reset"
                                );
                            } else {
                                c.getSource().sendMessage(
                                        Text.format("Next RW reset int {0, time, -timestamp}", next)
                                );
                            }

                            return 0;
                        })
                )

                .then(fieldArgument(RwField.ENABLED))
                .then(fieldArgument(RwField.RESET_END_MESSAGE))
                .then(fieldArgument(RwField.RESET_START_MESSAGE))
                .then(fieldArgument(RwField.TO_HAZ_GATE))
                .then(fieldArgument(RwField.TO_RES_GATE))
                .then(fieldArgument(RwField.WG_REGION_NAME))
                .then(fieldArgument(RwField.SIZE))
                .then(fieldArgument(RwField.SPAWN_STRUCTURE));
    }

    private <T> LiteralArgumentBuilder<CommandSource> fieldArgument(RwField<T> acc) {
        return literal(acc.getName())
                .executes(c -> {

                    c.getSource().sendMessage(
                            Component.text("[RW] ")
                                    .append(Component.text(acc.getName() + ": "))
                                    .append(acc.display())
                    );
                    return 0;
                })

                .then(argument("val", acc.getArgumentType())
                        .suggests(acc)

                        .executes(c -> {
                            T val = c.getArgument("val", acc.getTypeClass());

                            acc.set(val);

                            c.getSource().sendAdmin(
                                    Component.text("[RW] Set " + acc.getName() + " to ")
                                            .append(acc.display())
                            );
                            return 0;
                        })
                );
    }

    private interface RwField<T> extends SuggestionProvider<CommandSource> {
        RwField<String> WG_REGION_NAME = new RwField<>() {
            @Override
            public Component display() {
                return Component.text(ResourceWorldConfig.worldGuardSpawn);
            }

            @Override
            public void set(String val) throws CommandSyntaxException {
                ResourceWorldConfig.worldGuardSpawn = val;
            }

            @Override
            public ArgumentType<String> getArgumentType() {
                return StringArgumentType.word();
            }

            @Override
            public String getName() {
                return "world_guard_spawn";
            }

            @Override
            public Class<String> getTypeClass() {
                return String.class;
            }

            @Override
            public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
                World world = Worlds.resource();

                return CompletionProvider.suggestMatching(builder,
                        WorldGuard.getInstance()
                                .getPlatform()
                                .getRegionContainer()
                                .get(BukkitAdapter.adapt(world))
                                .getRegions().keySet()
                );
            }
        };

        RwField<Component> RESET_START_MESSAGE = new RwComponentField() {
            @Override
            public Component display() {
                return ResourceWorldConfig.resetStart;
            }

            @Override
            public void set(Component val) {
                ResourceWorldConfig.resetStart = val;
            }

            @Override
            public String getName() {
                return "reset_start_msg";
            }
        };

        RwField<Component> RESET_END_MESSAGE = new RwComponentField() {
            @Override
            public Component display() {
                return ResourceWorldConfig.resetEnd;
            }

            @Override
            public void set(Component val) {
                ResourceWorldConfig.resetEnd = val;
            }

            @Override
            public String getName() {
                return "reset_end_msg";
            }
        };

        RwField<String> TO_RES_GATE = new RwGateField() {
            @Override
            public Component display() {
                return Component.text(ResourceWorldConfig.toResGate);
            }

            @Override
            public void set(String val) throws CommandSyntaxException {
                validate(val);
                ResourceWorldConfig.toResGate = val;
            }

            @Override
            public String getName() {
                return "haz_to_res_gate";
            }
        };

        RwField<String> TO_HAZ_GATE = new RwGateField() {
            @Override
            public Component display() {
                return Component.text(ResourceWorldConfig.toHazGate);
            }

            @Override
            public void set(String val) throws CommandSyntaxException {
                validate(val);
                ResourceWorldConfig.toHazGate = val;
            }

            @Override
            public String getName() {
                return "res_to_haz_gate";
            }
        };

        RwField<Holder<BlockStructure>> SPAWN_STRUCTURE = new RwField<>() {
            @Override
            public Component display() {
                return Component.text(ResourceWorldConfig.spawnStructure);
            }

            @Override
            public void set(Holder<BlockStructure> val) throws CommandSyntaxException {
                ResourceWorldConfig.spawnStructure = val.getKey();
            }

            @Override
            public ArgumentType<Holder<BlockStructure>> getArgumentType() {
                return RegistryArguments.STRUCTURE;
            }

            @Override
            public String getName() {
                return "spawn_structure";
            }

            @Override
            public Class<Holder<BlockStructure>> getTypeClass() {
                Class holderClass = Holder.class;
                return holderClass;
            }

            @Override
            public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
                return RegistryArguments.STRUCTURE.listSuggestions(context, builder);
            }
        };

        RwField<Boolean> ENABLED = new RwField<>() {
            @Override
            public Component display() {
                return Component.text(ResourceWorldConfig.enabled);
            }

            @Override
            public void set(Boolean val) throws CommandSyntaxException {
                ResourceWorldConfig.enabled = val;
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

        RwField<Integer> SIZE = new RwField<>() {
            @Override
            public Component display() {
                return Component.text(ResourceWorldConfig.nextSize);
            }

            @Override
            public void set(Integer val) throws CommandSyntaxException {
                ResourceWorldConfig.nextSize = val;
            }

            @Override
            public ArgumentType<Integer> getArgumentType() {
                return IntegerArgumentType.integer(1, Level.MAX_LEVEL_SIZE - 6);
            }

            @Override
            public String getName() {
                return "next_size";
            }

            @Override
            public Class<Integer> getTypeClass() {
                return Integer.class;
            }

            @Override
            public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
                return CompletionProvider.suggestMatching(builder, "1600", "2000", "3000", "4000");
            }
        };

        @Override
        default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return Suggestions.empty();
        }

        Component display();
        void set(T val) throws CommandSyntaxException;

        ArgumentType<T> getArgumentType();
        String getName();
        Class<T> getTypeClass();
    }

    private interface RwComponentField extends RwField<Component> {
        @Override
        default Class<Component> getTypeClass() {
            return Component.class;
        }

        @Override
        default ArgumentType<Component> getArgumentType() {
            return Arguments.CHAT;
        }
    }

    private interface RwGateField extends RwField<String> {
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

            if (g == null) {
                throw Exceptions.invalidGate(val);
            }
        }
    }
}
package net.forthecrown.commands.admin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import net.forthecrown.useables.*;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.useables.actions.UsageActionInstance;
import net.forthecrown.useables.checks.UsageCheck;
import net.forthecrown.useables.checks.UsageCheckInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandInteractable extends FtcCommand {
    public static final StringReader EMPTY = new StringReader(" ");

    private static CommandInteractable instance;

    public CommandInteractable(){
        super("interactable", Crown.inst());

        instance = this;

        setAliases("usable");
        setPermission(Permissions.ADMIN);
        register();
    }

    public static CommandInteractable getInstance() {
        return instance;
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("block")
                        .then(argument("location", PositionArgument.blockPos())
                                .then(literal("create")
                                        .executes(c -> {
                                            CommandSource source = c.getSource();
                                            Location l = c.getArgument("location", Position.class).getLocation(source);

                                            if(!(l.getBlock().getState() instanceof TileState)) throw FtcExceptionProvider.create("Block is not sign");
                                            if(Crown.getUsables().isInteractableSign(l.getBlock())) throw FtcExceptionProvider.create("Block is already an interactable sign");

                                            Crown.getUsables().createBlock((TileState) l.getBlock().getState());
                                            c.getSource().sendAdmin("Creating interactable block");
                                            return 0;
                                        })
                                )

                                .then(editArg(CommandInteractable::block))
                                .then(removeArg(CommandInteractable::block))
                        )
                )
                .then(literal("entity")
                        .then(argument("selector", EntityArgument.entity())
                                .then(literal("create")
                                        .executes(c -> {
                                            CommandSource source = c.getSource();
                                            Entity entity = c.getArgument("selector", EntitySelector.class).getEntity(source);

                                            if(entity instanceof Player) throw FtcExceptionProvider.create("Players cannot be interactable");
                                            if(Crown.getUsables().isInteractableEntity(entity)) throw FtcExceptionProvider.create("Entity is already interactable");

                                            Crown.getUsables().createEntity(entity);

                                            c.getSource().sendAdmin("Creating interactable entity");
                                            return 0;
                                        })
                                )

                                .then(editArg(CommandInteractable::entity))
                                .then(removeArg(CommandInteractable::entity))
                        )
                );
    }

    private LiteralArgumentBuilder<CommandSource> editArg(InteractionUtils.BrigadierFunction<Usable> supplier){
        return literal("edit")
                .then(actionsArgument(supplier::apply))
                .then(checksArgument(supplier::apply))

                .then(literal("sendFail")
                        .then(argument("bool", BoolArgumentType.bool())
                                .executes(c -> {
                                    Usable sign = supplier.apply(c);
                                    boolean bool = c.getArgument("bool", Boolean.class);

                                    sign.setSendFail(bool);

                                    c.getSource().sendAdmin("Interaction check will send failure messages: " + bool);
                                    return 0;
                                })
                        )
                );
    }
    private LiteralArgumentBuilder<CommandSource> removeArg(InteractionUtils.BrigadierFunction<Usable> supplier){
        return literal("remove")
                .executes(c -> {
                    Usable interactable = supplier.apply(c);

                    interactable.delete();

                    c.getSource().sendAdmin("Deleting Interactable");
                    return 0;
                });
    }

    private static UsableEntity entity(CommandContext<CommandSource> c) throws CommandSyntaxException {
        Entity entity = c.getArgument("selector", EntitySelector.class).getEntity(c.getSource());
        if(!Crown.getUsables().isInteractableEntity(entity)) throw FtcExceptionProvider.create("Given entity is not an interactable entity");

        return Crown.getUsables().getEntity(entity);
    }
    private static UsableBlock block(CommandContext<CommandSource> c) throws CommandSyntaxException {
        Location l = c.getArgument("location", Position.class).getLocation(c.getSource());
        Block b = l.getBlock();
        if(!Crown.getUsables().isInteractableSign(b)) throw FtcExceptionProvider.create("Specified location is not an interactable sign");

        return Crown.getUsables().getBlock(b.getLocation());
    }

    public LiteralArgumentBuilder<CommandSource> checksArgument(InteractionUtils.BrigadierFunction<Checkable> p) {
        LiteralArgumentBuilder<CommandSource> result = literal("checks");
        addArguments(result, p, UsableAccessor.CHECKABLE);

        return result
                .then(literal("remove")
                        .then(argument("usageCheck", RegistryArguments.usageCheck())
                                .executes(c -> {
                                    Checkable checkable = p.apply(c);
                                    UsageCheck check = RegistryArguments.getCheck(c, "usageCheck");

                                    checkable.removeCheck(check.key());

                                    c.getSource().sendAdmin("Removed check");
                                    return 0;
                                })
                        )
                );
    }
    public LiteralArgumentBuilder<CommandSource> actionsArgument(InteractionUtils.BrigadierFunction<Actionable> p) {
        LiteralArgumentBuilder<CommandSource> result = literal("actions");
        addArguments(result, p, UsableAccessor.ACTIONABLE);

        return result
                .then(literal("remove")
                        .then(argument("index", IntegerArgumentType.integer(0))
                                .executes(c -> {
                                    Actionable actionable = p.apply(c);
                                    int index = c.getArgument("index", Integer.class);

                                    actionable.removeAction(index);

                                    c.getSource().sendAdmin("Removed action");
                                    return 0;
                                })
                        )
                );
    }

    private <T extends UsageType, V extends UsableObject> void addArguments(LiteralArgumentBuilder<CommandSource> args,
                                                                            InteractionUtils.BrigadierFunction<V> p,
                                                                            UsableAccessor<T ,V> acc
    ) {
        args
                .then(literal("list")
                        .executes(c -> {
                            V val = p.apply(c);

                            List<UsageTypeInstance> list = acc.listProper(val);
                            int index = 0;
                            TextComponent.Builder builder = Component.text()
                                    .append(Component.text(acc.getName() + "s: "));

                            for (UsageTypeInstance i: list) {
                                builder
                                        .append(Component.newline())
                                        .append(Component.text(index++ + ") " + i.asString()));
                            }

                            c.getSource().sendMessage(builder.build());
                            return 0;
                        })
                )

                .then(literal("add")
                        .then(argument("value", acc.getTypeArgument())
                                .executes(c -> {
                                    T val = acc.getType("value", c);
                                    V holder = p.apply(c);

                                    add(val, holder, EMPTY, c.getSource(), acc);
                                    return 0;
                                })

                                .then(argument("parseText", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> {
                                            T val = acc.getType("value", context);

                                            if(val == null) return Suggestions.empty();
                                            return val.getSuggestions(context, builder);
                                        })

                                        .executes(c -> {
                                            T val = (T) acc.getType("value", c);
                                            V holder = (V) p.apply(c);

                                            String parseText = (String) c.getArgument("parseText", String.class);
                                            StringReader parseReader = new StringReader(parseText);

                                            add(val, holder, parseReader, (CommandSource) c.getSource(), acc);
                                            return 0;
                                        })
                                )
                        )
                )

                .then(literal("clear")
                        .executes(c -> {
                            V holder = p.apply(c);
                            acc.clear(holder);

                            c.getSource().sendAdmin("Cleared all " + acc.getName() + "s");
                            return 0;
                        })
                );
    }

    <T extends UsageType, V extends UsableObject> void add(T val, V holder,
                                                           StringReader reader,
                                                           CommandSource source,
                                                           UsableAccessor<T, V> acc
    ) throws CommandSyntaxException {
        if(val.requiresInput() && reader == EMPTY) {
            throw expectedSeparator();
        }

        acc.add(holder, val, reader, source);

        if(val.requiresInput() && reader.canRead()) {
            throw expectedSeparator();
        }

        source.sendAdmin("Added " + acc.getName() + ": " + val.key().asString());
    }

    CommandSyntaxException expectedSeparator() {
        return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().create();
    }

    public interface UsableAccessor<T extends UsageType, V extends UsableObject> {
        UsableAccessor<UsageAction, Actionable> ACTIONABLE = new UsableAccessor<>() {
            @Override
            public ArgumentType<UsageAction> getTypeArgument() {
                return RegistryArguments.usageAction();
            }

            @Override
            public UsageAction getType(String argName, CommandContext<CommandSource> c) throws CommandSyntaxException {
                return RegistryArguments.getAction(c, argName);
            }

            @Override
            public void clear(Actionable holder) {
                holder.clearActions();
            }

            @Override
            public List<? extends UsageTypeInstance> list(Actionable holder) {
                return holder.getActions();
            }

            @Override
            public void add(Actionable holder, UsageAction val, StringReader reader, CommandSource source) throws CommandSyntaxException {
                holder.addAction((UsageActionInstance) val.parse(reader, source));
            }

            @Override
            public String getName() {
                return "Action";
            }
        };

        UsableAccessor<UsageCheck, Checkable> CHECKABLE = new UsableAccessor<>() {
            @Override
            public ArgumentType<UsageCheck> getTypeArgument() {
                return RegistryArguments.usageCheck();
            }

            @Override
            public UsageCheck getType(String argName, CommandContext<CommandSource> c) throws CommandSyntaxException {
                return RegistryArguments.getCheck(c, argName);
            }

            @Override
            public void clear(Checkable holder) {
                holder.clearChecks();
            }

            @Override
            public List<? extends UsageTypeInstance> list(Checkable holder) {
                return holder.getChecks();
            }

            @Override
            public void add(Checkable holder, UsageCheck val, StringReader reader, CommandSource source) throws CommandSyntaxException {
                holder.addCheck((UsageCheckInstance) val.parse(reader, source));
            }

            @Override
            public String getName() {
                return "Check";
            }
        };

        ArgumentType<T> getTypeArgument();
        T getType(String argName, CommandContext<CommandSource> c) throws CommandSyntaxException;

        void clear(V holder);

        default List<UsageTypeInstance> listProper(V holder) {
            return (List<UsageTypeInstance>) list(holder);
        }

        List<? extends UsageTypeInstance> list(V holder);
        void add(V holder, T val, StringReader reader, CommandSource source) throws CommandSyntaxException;

        String getName();
    }
}

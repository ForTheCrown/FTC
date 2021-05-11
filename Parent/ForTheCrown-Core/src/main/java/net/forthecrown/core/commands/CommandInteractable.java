package net.forthecrown.core.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Interactable;
import net.forthecrown.core.api.InteractableEntity;
import net.forthecrown.core.api.InteractableSign;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.commands.brigadier.types.SignActionType;
import net.forthecrown.core.commands.brigadier.types.SignPreconditionType;
import net.forthecrown.core.types.interactable.InteractionAction;
import net.forthecrown.core.types.interactable.InteractionCheck;
import net.forthecrown.core.types.interactable.UseablesManager;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CommandInteractable extends CrownCommandBuilder {

    public CommandInteractable(){
        super("interactable", FtcCore.getInstance());

        setAliases("useable");
        setPermission("ftc.admin");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("sign")
                        .then(argument("location", PositionArgument.position())
                                .then(argument("create")
                                        .executes(c -> {
                                            CommandSource source = c.getSource();
                                            Location l = c.getArgument("location", Position.class).getLocation(source);

                                            if(!(l.getBlock().getState() instanceof Sign)) throw FtcExceptionProvider.create("Block is not sign");
                                            if(UseablesManager.isInteractableSign(l.getBlock())) throw FtcExceptionProvider.create("Block is already an interactable sign");

                                            UseablesManager.createSign((Sign) l.getBlock().getState());
                                            c.getSource().sendAdmin("Creating interactable sign");
                                            return 0;
                                        })
                                )

                                .then(editArg(this::getSign))
                                .then(removeArg(this::getSign))
                        )
                )
                .then(argument("entity")
                        .then(argument("selector", EntityArgument.entity())
                                .then(argument("create")
                                        .executes(c -> {
                                            CommandSource source = c.getSource();
                                            Entity entity = c.getArgument("selector", EntitySelector.class).getEntity(source);

                                            if(entity instanceof Player) throw FtcExceptionProvider.create("Players cannot be interactable");
                                            if(UseablesManager.isInteractableEntity(entity)) throw FtcExceptionProvider.create("Entity is already interactable");

                                            UseablesManager.createEntity(entity);

                                            c.getSource().sendAdmin("Creating interactable entity");
                                            return 0;
                                        })
                                )

                                .then(editArg(this::entity))
                                .then(removeArg(this::entity))
                        )
                );
    }

    private LiteralArgumentBuilder<CommandSource> editArg(UsableSupplier supplier){
        return argument("edit")
                .then(argument("actions")
                        .then(argument("list")
                                .executes(c -> {
                                    Interactable sign = supplier.get(c);

                                    int index = 0;
                                    TextComponent.Builder builder = Component.text().append(Component.text("Interaction actions:"));

                                    for (InteractionAction p: sign.getActions()){
                                        builder.append(Component.newline());
                                        builder.append(Component.text(index + ") " + p.asString()));
                                        index++;
                                    }

                                    c.getSource().sendMessage(builder.build());
                                    return 0;
                                })
                        )

                        .then(argument("remove")
                                .then(argument("index", IntegerArgumentType.integer(0))
                                        .executes(c -> {
                                            Interactable sign = supplier.get(c);
                                            int index = c.getArgument("index", Integer.class);

                                            sign.removeAction(index);

                                            c.getSource().sendAdmin("Removed action with index " + index);
                                            return 0;
                                        })
                                )
                        )

                        .then(argument("add")
                                .then(argument("type", SignActionType.action())
                                        .then(argument("toParse", StringArgumentType.greedyString())
                                                .suggests((c, b) -> {
                                                    try {
                                                        return c.getArgument("type", InteractionAction.class).getSuggestions(c, b);
                                                    } catch (CommandSyntaxException ignored) {}
                                                    return Suggestions.empty();
                                                })

                                                .executes(c -> {
                                                    Interactable sign = supplier.get(c);
                                                    InteractionAction action = c.getArgument("type", InteractionAction.class);
                                                    String toParse = c.getArgument("toParse", String.class);

                                                    action.parse(c, new StringReader(toParse));
                                                    sign.addAction(action);

                                                    c.getSource().sendAdmin("Successfully added action");
                                                    return 0;
                                                })
                                        )
                                )
                        )

                        .then(argument("clear")
                                .executes(c -> {
                                    Interactable sign = supplier.get(c);
                                    sign.clearActions();

                                    c.getSource().sendAdmin("Cleared actions");
                                    return 0;
                                })
                        )
                )

                .then(argument("preconditions")
                        .then(argument("list")
                                .executes(c -> {
                                    Interactable sign = supplier.get(c);

                                    int index = 0;
                                    TextComponent.Builder builder = Component.text().append(Component.text("Interaction preconditions:"));

                                    for (InteractionCheck p: sign.getPreconditions()){
                                        builder.append(Component.newline());
                                        builder.append(Component.text(index + ") " + p.asString()));
                                        index++;
                                    }

                                    c.getSource().sendMessage(builder.build());
                                    return 0;
                                })
                        )

                        .then(argument("remove")
                                .then(argument("name", StringArgumentType.word())
                                        .suggests((c, b) -> {
                                            try {
                                                return CommandSource.suggestMatching(b, supplier.get(c).getPreconditionTypes());
                                            } catch (CommandSyntaxException ignored) {}
                                            return Suggestions.empty();
                                        })

                                        .executes(c -> {
                                            Interactable sign = supplier.get(c);
                                            String index = c.getArgument("index", String.class);

                                            sign.removePrecondition(index);

                                            c.getSource().sendAdmin("Removed precondition with index " + index);
                                            return 0;
                                        })
                                )
                        )

                        .then(argument("put")
                                .then(argument("type", SignPreconditionType.precondition())
                                        .then(argument("toParse", StringArgumentType.greedyString())
                                                .suggests((c, b) -> {
                                                    try {
                                                        return c.getArgument("type", InteractionCheck.class).getSuggestions(c, b);
                                                    } catch (CommandSyntaxException ignored) {}
                                                    return Suggestions.empty();
                                                })

                                                .executes(c -> {
                                                    Interactable sign = supplier.get(c);
                                                    InteractionCheck p = c.getArgument("type", InteractionCheck.class);

                                                    String toParse = c.getArgument("toParse", String.class);
                                                    p.parse(c, new StringReader(toParse));

                                                    sign.addPrecondition(p);

                                                    c.getSource().sendAdmin("Successfully added precondition");
                                                    return 0;
                                                })
                                        )
                                )
                        )

                        .then(argument("clear")
                                .executes(c -> {
                                    Interactable sign = supplier.get(c);
                                    sign.clearPreconditions();

                                    c.getSource().sendAdmin("Cleared sign preconditions");
                                    return 0;
                                })
                        )
                )

                .then(argument("sendFail")
                        .then(argument("bool", BoolArgumentType.bool())
                                .executes(c -> {
                                    Interactable sign = supplier.get(c);
                                    boolean bool = c.getArgument("bool", Boolean.class);

                                    sign.setSendFail(bool);

                                    c.getSource().sendAdmin("Interaction check will send failure messages: " + bool);
                                    return 0;
                                })
                        )
                );
    }

    private LiteralArgumentBuilder<CommandSource> removeArg(UsableSupplier supplier){
        return argument("remove")
                .executes(c -> {
                    Interactable interactable = supplier.get(c);

                    interactable.delete();

                    c.getSource().sendAdmin("Deleting Interactable");
                    return 0;
                });
    }

    private InteractableEntity entity(CommandContext<CommandSource> c) throws CommandSyntaxException {
        Entity entity = c.getArgument("selector", EntitySelector.class).getEntity(c.getSource());
        if(!UseablesManager.isInteractableEntity(entity)) throw FtcExceptionProvider.create("Given entity is not an interactable entity");

        return UseablesManager.getEntity(entity);
    }

    private InteractableSign getSign(CommandContext<CommandSource> c) throws CommandSyntaxException {
        Location l = c.getArgument("location", Position.class).getLocation(c.getSource());
        Block b = l.getBlock();
        if(!UseablesManager.isInteractableSign(b)) throw FtcExceptionProvider.create("Specified location is not an interactable sign");

        return UseablesManager.getSign(b.getLocation());
    }

    private interface UsableSupplier {
        Interactable get(CommandContext<CommandSource> context) throws CommandSyntaxException;
    }
}

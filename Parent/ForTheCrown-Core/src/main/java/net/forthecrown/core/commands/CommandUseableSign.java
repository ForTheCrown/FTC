package net.forthecrown.core.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownSign;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.commands.brigadier.types.SignActionType;
import net.forthecrown.core.commands.brigadier.types.SignPreconditionType;
import net.forthecrown.core.types.signs.SignAction;
import net.forthecrown.core.types.signs.SignManager;
import net.forthecrown.core.types.signs.SignPrecondition;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class CommandUseableSign extends CrownCommandBuilder {

    public CommandUseableSign(){
        super("useablesign", FtcCore.getInstance());

        setPermission("ftc.admin");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("location", PositionArgument.position())
                        .then(argument("create")
                                .executes(c -> {
                                    CommandSource source = c.getSource();
                                    Location l = c.getArgument("location", Position.class).getLocation(source);

                                    if(!(l.getBlock().getState() instanceof Sign)) throw FtcExceptionProvider.create("Block is not sign");
                                    if(SignManager.isInteractableSign(l.getBlock())) throw FtcExceptionProvider.create("Block is already an interactable sign");

                                    SignManager.createSign((Sign) l.getBlock().getState());
                                    c.getSource().sendAdmin("Creating interactable sign");
                                    return 0;
                                })
                        )

                        .then(argument("edit")
                                .then(argument("actions")
                                        .then(argument("list")
                                                .executes(c -> {
                                                    CrownSign sign = getSign(c);

                                                    int index = 0;
                                                    TextComponent.Builder builder = Component.text().append(Component.text("Sign actions:"));

                                                    for (SignAction p: sign.getActions()){
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
                                                            CrownSign sign = getSign(c);
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
                                                                        return c.getArgument("type", SignAction.class).getSuggestions(c, b);
                                                                    } catch (CommandSyntaxException ignored) {}
                                                                    return Suggestions.empty();
                                                                })

                                                                .executes(c -> {
                                                                    CrownSign sign = getSign(c);
                                                                    SignAction action = c.getArgument("type", SignAction.class);
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
                                                    CrownSign sign = getSign(c);
                                                    sign.clearActions();

                                                    c.getSource().sendAdmin("Cleared actions");
                                                    return 0;
                                                })
                                        )
                                )

                                .then(argument("preconditions")
                                        .then(argument("list")
                                                .executes(c -> {
                                                    CrownSign sign = getSign(c);

                                                    int index = 0;
                                                    TextComponent.Builder builder = Component.text().append(Component.text("Sign preconditions:"));

                                                    for (SignPrecondition p: sign.getPreconditions()){
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
                                                                return CommandSource.suggestMatching(b, getSign(c).getPreconditionTypes());
                                                            } catch (CommandSyntaxException ignored) {}
                                                            return Suggestions.empty();
                                                        })

                                                        .executes(c -> {
                                                            CrownSign sign = getSign(c);
                                                            String index = c.getArgument("index", String.class);

                                                            sign.removePrecondition(index);

                                                            c.getSource().sendAdmin("Removed precondition with index " + index);
                                                            return 0;
                                                        })
                                                )
                                        )

                                        .then(argument("add")
                                                .then(argument("type", SignPreconditionType.precondition())
                                                        .then(argument("toParse", StringArgumentType.greedyString())
                                                                .suggests((c, b) -> {
                                                                    try {
                                                                        return c.getArgument("type", SignPrecondition.class).getSuggestions(c, b);
                                                                    } catch (CommandSyntaxException ignored) {}
                                                                    return Suggestions.empty();
                                                                })

                                                                .executes(c -> {
                                                                    CrownSign sign = getSign(c);
                                                                    SignPrecondition p = c.getArgument("type", SignPrecondition.class);

                                                                    String toParse = c.getArgument("toParse", String.class);
                                                                    p.parse(toParse);

                                                                    sign.addPrecondition(p);

                                                                    c.getSource().sendAdmin("Successfully added precondition");
                                                                    return 0;
                                                                })
                                                        )
                                                )
                                        )

                                        .then(argument("clear")
                                                .executes(c -> {
                                                    CrownSign sign = getSign(c);
                                                    sign.clearPreconditions();

                                                    c.getSource().sendAdmin("Cleared sign preconditions");
                                                    return 0;
                                                })
                                        )
                                )

                                .then(argument("sendFail")
                                        .then(argument("bool", BoolArgumentType.bool())
                                                .executes(c -> {
                                                    CrownSign sign = getSign(c);
                                                    boolean bool = c.getArgument("bool", Boolean.class);

                                                    sign.setSendFail(bool);

                                                    c.getSource().sendAdmin("Sign will send failure messages: " + bool);
                                                    return 0;
                                                })
                                        )
                                )
                        )

                        .then(argument("remove")
                                .executes(c -> {
                                    CrownSign sign = getSign(c);

                                    sign.delete();

                                    c.getSource().sendAdmin("Deleting interactable sign");
                                    return 0;
                                })
                        )
                );
    }

    private CrownSign getSign(CommandContext<CommandSource> c) throws CommandSyntaxException {
        Location l = c.getArgument("location", Position.class).getLocation(c.getSource());
        Block b = l.getBlock();
        if(!SignManager.isInteractableSign(b)) throw FtcExceptionProvider.create("Specified location is not an interactable sign");

        return SignManager.getSign(b.getLocation());
    }
}

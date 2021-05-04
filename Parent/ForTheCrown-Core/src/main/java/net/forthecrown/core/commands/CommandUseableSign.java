package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownSign;
import net.forthecrown.core.commands.brigadier.CoreCommands;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.types.signs.SignAction;
import net.forthecrown.core.types.signs.SignManager;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class CommandUseableSign extends CrownCommandBuilder {

    public CommandUseableSign(){
        super("useablesign", FtcCore.getInstance());

        setPermission("ftc.admin");
        register();
    }

    private static final EnumArgument<SignAction.Action> enumType = EnumArgument.of(SignAction.Action.class);

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("create")
                        .then(actionEditArg((c, type) -> {
                            try {
                                if(type == null) throw FtcExceptionProvider.create("Null action not allowed on creation");

                                Player player = getPlayerSender(c);
                                Block block = player.getTargetBlock(5);

                                if(block == null || !(block.getState() instanceof Sign))
                                    throw FtcExceptionProvider.create("You must be looking at a sign");

                                if(SignManager.isInteractableSign(block))
                                    throw FtcExceptionProvider.create("The sign is already an interactable sign");

                                SignManager.createSign((Sign) block.getState(), type);
                                broadcastAdmin(c, "Making interactable sign");
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            return 0;
                        }))
                )
                .then(argument("edit")
                        .then(argument("sendCooldown")
                                .then(argument("bool", BoolArgumentType.bool())
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            CrownSign sign = getSign(player);

                                            boolean value = c.getArgument("bool", Boolean.class);
                                            sign.setSendCooldownMessage(value);

                                            broadcastAdmin(c, "Sign will send cooldown messages: " + value);
                                            return 0;
                                        })
                                )
                        )
                        .then(argument("sendFail")
                                .then(argument("bool", BoolArgumentType.bool())
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            CrownSign sign = getSign(player);

                                            boolean value = c.getArgument("bool", Boolean.class);
                                            sign.setFailSendMessage(value);

                                            broadcastAdmin(c, "Sign will send failure messages: " + value);
                                            return 0;
                                        })
                                )
                        )

                        .then(argument("actions")
                                .then(argument("add")
                                        .then(actionEditArg((c, type) -> {
                                            Player player = getPlayerSender(c);
                                            CrownSign sign = getSign(player);
                                            sign.addAction(type);

                                            broadcastAdmin(c, "Added action " + type.getAction().toString().toLowerCase());
                                            return 0;
                                        }))
                                )
                                .then(argument("remove")
                                        .then(argument("index", IntegerArgumentType.integer(0))
                                                .executes(c -> {
                                                    Player player = getPlayerSender(c);
                                                    CrownSign sign = getSign(player);
                                                    int index = c.getArgument("index", Integer.class);

                                                    sign.removeAction(index);
                                                    broadcastAdmin(c, "Removing action");
                                                    return 0;
                                                })
                                        )
                                )

                                .then(argument("list")
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            CrownSign sign = getSign(player);

                                            player.sendMessage(Component.text("actions: " + sign.getActions().toString()));
                                            return 0;
                                        })
                                )
                                .then(argument("clear")
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            CrownSign sign = getSign(player);

                                            sign.clearActions();

                                            broadcastAdmin(c, "Clearing actions list");
                                            return 0;
                                        })
                                )
                        )

                        .then(argument("preconditions")
                                .then(preconditionArgument(IntPrecondition.COOLDOWN))
                                .then(preconditionArgument(IntPrecondition.REQUIRED_BALANCE))
                                .then(preconditionArgument(IntPrecondition.REQUIRED_GEMS))

                                .then(argument("required_rank")
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            CrownSign sign = getSign(player);

                                            sign.setRequiredRank(null);
                                            broadcastAdmin(c,"Required rank set to none");
                                            return 0;
                                        })

                                        .then(argument("rank", CoreCommands.RANK)
                                                .executes(c -> {
                                                    Player player = getPlayerSender(c);
                                                    CrownSign sign = getSign(player);
                                                    Rank rank = c.getArgument("rank", Rank.class);

                                                    sign.setRequiredRank(rank);

                                                    broadcastAdmin(c, "Set required rank to " + rank.getPrefix());
                                                    return 0;
                                                })
                                        )
                                )

                                .then(argument("required_branch")
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            CrownSign sign = getSign(player);

                                            sign.setRequiredBranch(null);
                                            broadcastAdmin(c,"Required branch set to none");
                                            return 0;
                                        })

                                        .then(argument("branch", CoreCommands.BRANCH)
                                                .executes(c -> {
                                                    Player player = getPlayerSender(c);
                                                    CrownSign sign = getSign(player);
                                                    Branch branch = c.getArgument("branch", Branch.class);

                                                    sign.setRequiredBranch(branch);

                                                    broadcastAdmin(c.getSource(), "Set required branch to " + branch.getName());
                                                    return 0;
                                                })
                                        )
                                )

                                .then(argument("required_permission")
                                        .executes(c -> {
                                            Player player = getPlayerSender(c);
                                            CrownSign sign = getSign(player);

                                            sign.setRequiredPermission(null);
                                            broadcastAdmin(c.getSource(),"Required permission set to none");
                                            return 0;
                                        })

                                        .then(argument("permission", StringArgumentType.word())
                                                .executes(c -> {
                                                    Player player = getPlayerSender(c);
                                                    CrownSign sign = getSign(player);
                                                    String permission = c.getArgument("permission", String.class);

                                                    sign.setRequiredPermission(permission);

                                                    broadcastAdmin(c.getSource(), "Set required permission to " + permission);
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )
                .then(argument("remove")
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            CrownSign sign = getSign(player);

                            sign.delete();

                            broadcastAdmin(c.getSource(), "Deleting sign");
                            return 0;
                        })
                );
    }

    private CrownSign getSign(Player player) throws CommandSyntaxException {
        Block block = player.getTargetBlock(5);

        if(!SignManager.isInteractableSign(block)) throw FtcExceptionProvider.create("You must be looking at an interactable sign");
        return SignManager.getSign(block.getLocation());
    }

    private RequiredArgumentBuilder<CommandSource , SignAction.Action> actionEditArg(ActionCommand command){
        return argument("action", enumType)
                .then(argument("toParse", StringArgumentType.greedyString())
                        .executes(c -> {
                            SignAction.Action action = c.getArgument("action", SignAction.Action.class);
                            SignAction type = action.get();
                            type.parse(c.getArgument("toParse", String.class));

                            return command.run(c, type);
                        })
                );
    }

    private interface ActionCommand{
        int run(CommandContext<CommandSource> c, SignAction type) throws CommandSyntaxException;
    }

    private LiteralArgumentBuilder<CommandSource> preconditionArgument(IntPrecondition precondition){
        return argument(precondition.toString().toLowerCase())
                .then(argument("amount", IntegerArgumentType.integer())
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            CrownSign sign = getSign(player);
                            int amount = c.getArgument("amount", Integer.class);

                            precondition.consumer.accept(sign, amount);

                            broadcastAdmin(c.getSource(), "Changed precondition " + precondition.toString().toLowerCase() + " to " + amount);
                            return 0;
                        })
                );
    }

    public enum IntPrecondition {
        COOLDOWN (CrownSign::setCooldown),
        REQUIRED_BALANCE (CrownSign::setRequiredBal),
        REQUIRED_GEMS (CrownSign::setRequiredGems);

        private final BiConsumer<CrownSign, Integer> consumer;
        IntPrecondition(BiConsumer<CrownSign, Integer> consumer){
            this.consumer = consumer;
        }
    }
}

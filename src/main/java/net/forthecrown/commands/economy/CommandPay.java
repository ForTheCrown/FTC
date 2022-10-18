package net.forthecrown.commands.economy;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Vars;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.core.admin.Mute;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.royalgrenadier.types.selector.EntityArgumentImpl;
import net.forthecrown.text.Messages;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.kyori.adventure.text.Component;

import java.util.List;

import static net.forthecrown.text.Messages.PAY_BLOCKED;

public class CommandPay extends FtcCommand {

    public CommandPay(){
        super("pay");

        setDescription("Pay another player");
        setPermission(Permissions.PAY);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Pays another player a set amount of money, removes the money from the player as well
     *
     * Valid usages of command:
     * - /pay <player> <amount> [message]
     *
     * Author: Julie
     */

    static final int[] BALANCE_SUGGESTIONS = {
            1, 5,
            10, 50,
            100, 500,
            1000, 5000,
            10000, 50000,
    };

    public static final SuggestionProvider<CommandSource> BAL_SUGGESTIONS = (context, builder) -> {
        if (!context.getSource().isPlayer()) {
            return Suggestions.empty();
        }

        var user = getUserSender(context);
        var token = builder.getRemainingLowerCase();

        // Suggest fixed balance amounts
        for (int i: BALANCE_SUGGESTIONS) {
            if (!user.hasBalance(i)) {
                continue;
            }

            if (!CompletionProvider.startsWith(token, i + "")) {
                continue;
            }

            builder.suggest(i);
        }

        // Suggest user's balance
        var balance = user.getBalance();
        if (CompletionProvider.startsWith(token, balance + "")) {
            builder.suggest(balance);
        }

        return builder.buildFuture();
    };

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("players", Arguments.USERS)

                        // /pay <user> <amount>
                      .then(argument("amount", IntegerArgumentType.integer(1, Vars.maxMoneyAmount))

                              // Suggest fixed rhine amount and/or
                              // the sender's balance amount
                              .suggests(BAL_SUGGESTIONS)

                              .executes(c -> {
                                  User user = getUserSender(c);
                                  int amount = c.getArgument("amount", Integer.class);
                                  List<User> users = Arguments.getUsers(c, "players");

                                  return pay(user, users, amount, null);
                              })

                              // /pay <user> <amount> <message>
                              .then(argument("message", Arguments.MESSAGE)
                                      .executes(c -> {
                                          User user = getUserSender(c);

                                          int amount = c.getArgument("amount", Integer.class);
                                          List<User> users = Arguments.getUsers(c, "players");
                                          Component message = Arguments.getMessage(c, "message");

                                          return pay(user, users, amount, message);
                                      })
                              )
                      )
                );
    }

    int pay(User user, List<User> targets, int amount, Component message) throws CommandSyntaxException {
        if (!user.get(Properties.PAY)) {
            throw Exceptions.SENDER_PAY_DISABLED;
        }

        if (targets.size() == 1) {
            User target = targets.get(0);

            if (!user.hasBalance(amount)) {
                throw Exceptions.cannotAfford(amount);
            }

            if (Users.testBlocked(user, target,
                    PAY_BLOCKED, PAY_BLOCKED
            )) {
                return 0;
            }
        }

        targets.removeIf(target -> {
            if (Users.areBlocked(user, target)) {
                return true;
            }

            return !target.get(Properties.PAY);
        });

        if (targets.remove(user) && targets.isEmpty()) {
            throw Exceptions.CANNOT_PAY_SELF;
        }

        if (targets.isEmpty()) {
            throw EntityArgumentImpl.NO_ENTITIES_FOUND.create();
        }

        int total = amount * targets.size();
        if (!user.hasBalance(total)) {
            throw Exceptions.cannotAfford(amount * targets.size());
        }

        byte paidAmount = 0;

        Mute status = Punishments.muteStatus(user);
        Component selfMessage;

        if (message == null
                || BannedWords.contains(message)
        ) {
            message = null;
            selfMessage = null;
        } else {
            if (!status.isVisibleToOthers()) {
                message = null;
            }

            if (status.isVisibleToSender()) {
                selfMessage = message;
            } else {
                selfMessage = null;
            }
        }

        for (User target: targets) {
            target.addBalance(amount);
            user.removeBalance(amount);

            user.sendMessage(Messages.paySender(target, amount, selfMessage));
            target.sendMessage(Messages.payTarget(user, amount, message));

            paidAmount++;
            target.unloadIfOffline();
        }

        if (paidAmount > 1) {
            user.sendMessage(Messages.paidMultiple(paidAmount, amount));
        }
        return 0;
    }
}
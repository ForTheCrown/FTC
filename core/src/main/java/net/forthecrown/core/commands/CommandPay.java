package net.forthecrown.core.commands;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.Objects;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.core.PrefsBook;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.channel.MessageHandler;
import net.forthecrown.user.User;
import net.forthecrown.user.UserBlockList;
import org.bukkit.Sound;

public class CommandPay extends FtcCommand {

  public CommandPay() {
    super("pay");

    setDescription("Pay another player");
    setPermission(CorePermissions.PAY);

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
    for (int i : BALANCE_SUGGESTIONS) {
      if (!user.hasBalance(i)) {
        continue;
      }

      String suggestion;

      if (i < 1000) {
        suggestion = String.valueOf(i);
      } else {
        suggestion = i / 1000 + "k";
      }

      if (!Completions.matches(token, suggestion)) {
        continue;
      }

      builder.suggest(suggestion);
    }

    // Suggest user's balance
    var balance = user.getBalance();
    if (Completions.matches(token, balance + "")) {
      builder.suggest(balance);
    }

    return builder.buildFuture();
  };

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<players> <amount: number(1..)> [<message>]")
        .addInfo("Pays all <players> an <amount> of rhines [message] specifies")
        .addInfo("an optional message to send to players being paid.");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("players", Arguments.USERS)

            // /pay <user> <amount>
            .then(argument("amount", Arguments.RHINES)

                // Suggest fixed rhine amount and/or
                // the sender's balance amount
                .suggests(BAL_SUGGESTIONS)

                .executes(c -> {
                  User user = getUserSender(c);
                  int amount = c.getArgument("amount", Integer.class);
                  List<User> users = Arguments.getUsers(c, "players");

                  return pay(c, user, users, amount, null);
                })

                // /pay <user> <amount> <message>
                .then(argument("message", Arguments.MESSAGE)
                    .executes(c -> {
                      User user = getUserSender(c);

                      int amount = c.getArgument("amount", Integer.class);
                      List<User> users = Arguments.getUsers(c, "players");
                      ViewerAwareMessage message = Arguments.getMessage(c, "message");

                      return pay(c, user, users, amount, message);
                    })
                )
            )
        );
  }

  int pay(
      CommandContext<CommandSource> c,
      User user,
      List<User> targets,
      int amount,
      ViewerAwareMessage message
  ) throws CommandSyntaxException {
    if (!user.get(PrefsBook.PAY)) {
      throw CoreExceptions.SENDER_PAY_DISABLED;
    }

    var denyReason = UserBlockList.filterPlayers(
        user,
        targets,
        PrefsBook.PAY,
        "Cannot pay blocked user: {0, user}",
        text("You cannot pay yourself.")
    );

    if (denyReason.isPresent()) {
      throw Exceptions.create(denyReason.get());
    }

    int total = amount * targets.size();
    if (!user.hasBalance(total)) {
      throw Exceptions.cannotAfford(amount * targets.size());
    }

    byte paidAmount = 0;

    ViewerAwareMessage actualMessage = Objects.requireNonNullElseGet(
        message, () -> ViewerAwareMessage.wrap(empty())
    );

    ChannelledMessage ch = ChannelledMessage.create(actualMessage);
    ch.setHandler(MessageHandler.EMPTY_IF_VIEWER_WAS_REMOVED);
    ch.addTargets(targets);
    ch.setChannelName("commands/pay");
    ch.shownToSource(false);
    ch.setRenderer((viewer, baseMessage) -> {
      return CoreMessages.payTarget(user, amount, baseMessage).create(viewer);
    });

    if (ch.send() == -1) {
      return 0;
    }

    for (User target : targets) {
      target.addBalance(amount);
      user.removeBalance(amount);

      user.sendMessage(CoreMessages.paySender(target, amount, actualMessage));
      target.playSound(Sound.UI_TOAST_IN, 2, 1.3f);

      paidAmount++;
    }

    if (paidAmount > 0) {
      user.playSound(Sound.UI_TOAST_OUT, 2, 1.5f);
    }

    if (paidAmount > 1) {
      user.sendMessage(CoreMessages.paidMultiple(paidAmount, amount));
    }

    return 0;
  }
}
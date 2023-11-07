package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.Permissions;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.core.CorePlugin;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Messages;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.UserLookup.LookupEntry;
import net.forthecrown.user.Users;

public class CommandNickname extends FtcCommand {

  public static final String CLEAR = "-clear";

  public CommandNickname() {
    super("nickname");

    setAliases("nick");
    setPermission(CorePermissions.CMD_NICKNAME);
    setDescription("Sets your nickname");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Clears your nickname");
    factory.usage("-clear", "Clears your nickname");
    factory.usage("<nick>", "Sets your nickname");
  }

  /**
   * Checks if the given nickname is allowed
   *
   * @param nick The nickname to check for
   * @throws CommandSyntaxException If the nickname is invalid
   */
  public static void checkNickAllowed(String nick) throws CommandSyntaxException {
    var config = CorePlugin.plugin().getFtcConfig();

    if (config.maxNickLength() < nick.length()) {
      throw Exceptions.format("Nickname '{0}' is too long (max length {1, number})",
          nick, config.maxNickLength()
      );
    }

    UserLookup cache = Users.getService().getLookup();
    LookupEntry entry = cache.query(nick);

    if (entry != null) {
      throw Exceptions.format("Nickname '{0}' is unavailable", nick);
    }
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        // /nick -> clears nick
        .executes(c -> {
          User user = getUserSender(c);
          user.setNickname(null);

          user.sendMessage(CoreMessages.NICK_CLEARED);
          return 0;
        })

        // /nick <nickname> -> sets nickname or clears it
        .then(argument("nick", StringArgumentType.word())
            .suggests((context, builder) -> {
              return Completions.suggest(builder, CLEAR);
            })

            .executes(c -> {
              User user = getUserSender(c);
              String nick = c.getArgument("nick", String.class);
              checkNickAllowed(nick);

              if (nick.startsWith(Messages.DASH_CLEAR.content())) {
                nick = null;
              }

              if (nick == null) {
                user.sendMessage(CoreMessages.NICK_CLEARED);
              } else {
                if (nick.equals(user.getNickname())) {
                  throw CoreExceptions.ALREADY_YOUR_NICK;
                }

                user.sendMessage(CoreMessages.nickSetSelf(nick));
              }

              user.setNickname(nick);
              return 0;
            })

            // /nick <nickname> <user> -> sets/clears nickname for given user
            .then(argument("user", Arguments.USER)
                .requires(s -> s.hasPermission(Permissions.ADMIN))

                .executes(c -> {
                  User user = Arguments.getUser(c, "user");
                  String nick = c.getArgument("nick", String.class);
                  checkNickAllowed(nick);

                  if (nick.startsWith(Messages.DASH_CLEAR.content())) {
                    nick = null;
                  }

                  boolean self = c.getSource().textName().equals(user.getName());

                  if (nick == null) {
                    c.getSource().sendSuccess(CoreMessages.nickClearOther(user));

                    if (user.isOnline() && !self) {
                      user.sendMessage(CoreMessages.NICK_CLEARED);
                    }
                  } else {
                    if (nick.equals(user.getNickname())) {
                      throw CoreExceptions.ALREADY_THEIR_NICK;
                    }

                    c.getSource().sendSuccess(CoreMessages.nickSetOther(user, nick));

                    if (user.isOnline() && !self) {
                      user.sendMessage(CoreMessages.nickSetSelf(nick));
                    }
                  }

                  user.setNickname(nick);
                  return 0;
                })
            )
        );
  }
}
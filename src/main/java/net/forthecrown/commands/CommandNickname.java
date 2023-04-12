package net.forthecrown.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.UserLookupEntry;
import net.forthecrown.user.UserManager;

public class CommandNickname extends FtcCommand {

  public static final String CLEAR = "-clear";

  public CommandNickname() {
    super("nickname");

    setAliases("nick");
    setPermission(Permissions.CMD_NICKNAME);
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
    if (GeneralConfig.maxNickLength < nick.length()) {
      throw Exceptions.nickTooLong(nick.length());
    }

    UserLookup cache = UserManager.get()
        .getUserLookup();

    UserLookupEntry entry = cache.get(nick);

    if (entry != null) {
      throw Exceptions.NICK_UNAVAILABLE;
    }
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        // /nick -> clears nick
        .executes(c -> {
          User user = getUserSender(c);
          user.setNickname(null);

          user.sendMessage(Messages.NICK_CLEARED);
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
                user.sendMessage(Messages.NICK_CLEARED);
              } else {
                if (nick.equals(user.getNickname())) {
                  throw Exceptions.ALREADY_YOUR_NICK;
                }

                user.sendMessage(Messages.nickSetSelf(nick));
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
                    c.getSource().sendSuccess(Messages.nickClearOther(user));

                    if (user.isOnline() && !self) {
                      user.sendMessage(Messages.NICK_CLEARED);
                    }
                  } else {
                    if (nick.equals(user.getNickname())) {
                      throw Exceptions.ALREADY_THEIR_NICK;
                    }

                    c.getSource().sendSuccess(Messages.nickSetOther(user, nick));

                    if (user.isOnline() && !self) {
                      user.sendMessage(Messages.nickSetSelf(nick));
                    }
                  }

                  user.setNickname(nick);
                  return 0;
                })
            )
        );
  }
}
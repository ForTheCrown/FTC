package net.forthecrown.command.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.forthecrown.command.arguments.chat.ChatArgument;
import net.forthecrown.command.arguments.chat.MessageArgument;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;

public interface Arguments {

  ChatArgument CHAT = new ChatArgument();

  MessageArgument MESSAGE = new MessageArgument();

  UserArgument USER = new UserArgument(false, true);
  UserArgument USERS = new UserArgument(true, true);
  UserArgument ONLINE_USERS = new UserArgument(true, false);
  UserArgument ONLINE_USER = new UserArgument(false, false);

  FtcKeyArgument FTC_KEY = new FtcKeyArgument();

  static List<User> getUsers(CommandContext<CommandSource> c, String argument)
      throws CommandSyntaxException
  {
    UserParseResult result = c.getArgument(argument, UserParseResult.class);
    return result.getUsers(c.getSource(), true);
  }

  static User getUser(CommandContext<CommandSource> c, String argument)
      throws CommandSyntaxException
  {
    UserParseResult result = c.getArgument(argument, UserParseResult.class);
    return result.get(c.getSource(), true);
  }

  static Component getMessage(CommandContext<CommandSource> c, String arg) {
    var source = c.getSource();
    return c.getArgument(arg, MessageArgument.Result.class).format(source.asBukkit());
  }
}
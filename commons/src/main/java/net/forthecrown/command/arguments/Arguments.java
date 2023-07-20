package net.forthecrown.command.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.forthecrown.McConstants;
import net.forthecrown.command.arguments.chat.ChatArgument;
import net.forthecrown.command.arguments.chat.MessageArgument;
import net.forthecrown.command.arguments.chat.MessageArgument.Result;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.SuffixedNumberArgument;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public interface Arguments {

  ChatArgument CHAT = new ChatArgument();

  MessageArgument MESSAGE = new MessageArgument();

  UserArgument USER = new UserArgument(false, true);
  UserArgument USERS = new UserArgument(true, true);
  UserArgument ONLINE_USERS = new UserArgument(true, false);
  UserArgument ONLINE_USER = new UserArgument(false, false);

  FtcKeyArgument FTC_KEY = new FtcKeyArgument();

  SuffixedNumberArgument<Integer> RHINES = createRhineArgument();

  SuffixedNumberArgument<Integer> GAMETIME = createGametimeArgument();

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

  static Result getUserMessage(CommandContext<CommandSource> c, String arg) {
    return c.getArgument(arg, Result.class);
  }

  static ViewerAwareMessage getMessage(CommandContext<CommandSource> c, String arg) {
    CommandSender source = c.getSource().asBukkit();

    try {
      Result result = c.getArgument(arg, Result.class);
      return viewer -> result.format(source, viewer);
    } catch (IllegalArgumentException exc) {
      if (exc.getMessage().startsWith("Argument '")) {
        return c.getArgument(arg, ViewerAwareMessage.class);
      }

      throw exc;
    }
  }

  private static SuffixedNumberArgument<Integer> createRhineArgument() {
    Map<String, Integer> units = new HashMap<>();
    units.put("k",    1000);
    units.put("m",    1_000_000);
    units.put("mil",  1_000_000);
    units.put("b",    1_000_000_000);
    units.put("bil",  1_000_000_000);

    return ArgumentTypes.suffixedInt(units);
  }

  private static SuffixedNumberArgument<Integer> createGametimeArgument() {
    Map<String, Integer> units = new HashMap<>();
    units.put("t", 1);
    units.put("s", 20);
    units.put("d", McConstants.TICKS_PER_DAY);
    return ArgumentTypes.suffixedInt(units);
  }
}
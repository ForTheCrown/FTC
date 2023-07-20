package net.forthecrown.core.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandReply extends FtcCommand {

  public CommandReply() {
    super("reply");

    setPermission(CorePermissions.MESSAGE);
    setAliases("er", "ereply", "respond", "r");
    setDescription("Send a message to the last person to send you a message");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<message>")
        .addInfo("Sends a <message> to the last person that")
        .addInfo("messaged you / you messaged.");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.then(argument("message", Arguments.MESSAGE)
        .executes(c -> {
          User user = getUserSender(c);
          CommandSource source = user.getLastMessage();

          if (source == null || !sourceIsOnline(source)) {
            throw CoreExceptions.NO_REPLY_TARGETS;
          }

          CommandTell.run(user.getCommandSource(), source, Arguments.getMessage(c, "message"));
          return 0;
        })
    );
  }

  private boolean sourceIsOnline(CommandSource source) throws CommandSyntaxException {
    if (!source.isPlayer()) {
      return true;
    }

    return source.asPlayer().isOnline();
  }
}
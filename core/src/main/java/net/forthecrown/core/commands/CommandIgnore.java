package net.forthecrown.core.commands;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.UserBlockList;
import net.forthecrown.user.UserBlockList.IgnoreResult;

public class CommandIgnore extends FtcCommand {

  public CommandIgnore() {
    super("ignore");

    setPermission(CorePermissions.IGNORE);
    setAliases("ignoreplayer", "unignore", "unignoreplayer", "block", "unblock");
    setDescription("Makes you ignore/unignore another player");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user>", "Ignores/unignores a <user>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.USER)
            .executes(c -> {
              User user = getUserSender(c);
              User target = Arguments.getUser(c, "user");

              if (target.equals(user)) {
                throw CoreExceptions.CANNOT_IGNORE_SELF;
              }

              UserBlockList list = user.getComponent(UserBlockList.class);
              boolean alreadyIgnoring = list.testIgnored(target) == IgnoreResult.BLOCKED;

              if (alreadyIgnoring) {
                user.sendMessage(CoreMessages.unignorePlayer(target));
                list.removeIgnored(target);
              } else {
                user.sendMessage(CoreMessages.ignorePlayer(target));
                list.setIgnored(target, false);
              }

              return 0;
            })
        );
  }
}

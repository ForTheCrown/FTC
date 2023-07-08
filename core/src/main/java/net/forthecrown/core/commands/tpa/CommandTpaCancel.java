package net.forthecrown.core.commands.tpa;

import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandTpaCancel extends FtcCommand {

  public CommandTpaCancel() {
    super("tpacancel");

    setPermission(TpPermissions.TPA);
    setDescription("Cancels a tpa request");
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.ONLINE_USER)
            .executes(c -> {
              User user = getUserSender(c);
              User target = Arguments.getUser(c, "user");

              TeleportRequest r = TeleportRequests.getOutgoing(user, target);
              if (r == null) {
                throw Exceptions.noOutgoing(target);
              }

              r.cancel();
              return 0;
            })
        )

        .executes(c -> {
          User user = getUserSender(c);
          TeleportRequest r = TeleportRequests.latestOutgoing(user);

          if (r == null) {
            throw TpExceptions.NO_TP_REQUESTS;
          }

          r.cancel();
          return 0;
        });
  }
}
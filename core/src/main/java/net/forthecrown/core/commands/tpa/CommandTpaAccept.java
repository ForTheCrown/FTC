package net.forthecrown.core.commands.tpa;

import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandTpaAccept extends FtcCommand {

  public CommandTpaAccept() {
    super("tpaccept");

    setPermission(TpPermissions.TPA);
    setDescription("Accepts a tpa request");
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.ONLINE_USER)
            .executes(c -> {
              User user = getUserSender(c);
              User sender = Arguments.getUser(c, "user");

              TeleportRequest r = TeleportRequests.getIncoming(user, sender);

              if (r == null) {
                throw Exceptions.noIncoming(sender);
              }

              r.accept();
              return 0;
            })
        )

        .executes(c -> {
          User user = getUserSender(c);
          TeleportRequest r = TeleportRequests.latestIncoming(user);

          if (r == null) {
            throw TpExceptions.NO_TP_REQUESTS;
          }

          r.accept();
          return 0;
        });
  }
}
package net.forthecrown.core.commands;

import net.forthecrown.Permissions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.user.User;

public class CommandAfk extends FtcCommand {

  public CommandAfk() {
    super("afk");

    setDescription("Marks or un-marks you as AFK");
    setPermission(Permissions.DEFAULT);
    setAliases("away");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Sets you afk/unafk");
    factory.usage("<message>", "AFKs you with an AFK message");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> afk(getUserSender(c), null))

        .then(literal("-other")
            .requires(s -> s.hasPermission(Permissions.ADMIN))

            .then(argument("user", Arguments.ONLINE_USER)
                .requires(s -> s.hasPermission(Permissions.ADMIN))

                .executes(c -> afk(
                    Arguments.getUser(c, "user"),
                    null
                ))
            )
        )

        .then(argument("msg", Arguments.MESSAGE)
            .executes(c -> afk(
                getUserSender(c),
                Arguments.getMessage(c, "msg")
            ))
        );
  }

  private int afk(User user, ViewerAwareMessage message) {
    boolean alreadyAFK = user.isAfk();

    if (alreadyAFK) {
      user.unafk();
    } else {
      user.afk(message);
    }

    return 0;
  }
}
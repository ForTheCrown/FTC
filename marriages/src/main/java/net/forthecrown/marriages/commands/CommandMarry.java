package net.forthecrown.marriages.commands;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.marriages.MPermissions;
import net.forthecrown.marriages.requests.Proposals;
import net.forthecrown.user.User;

public class CommandMarry extends FtcCommand {

  public CommandMarry() {
    super("marry");

    setDescription("Marry a person");
    setPermission(MPermissions.MARRY);
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /marry
   *
   * Permissions used:
   * ftc.marry
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<player>", "Propose to a <player>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.ONLINE_USER)
            .executes(c -> {
              User user = getUserSender(c);
              User target = Arguments.getUser(c, "user");

              Proposals.propose(user, target);
              return 0;
            })
        );
  }
}
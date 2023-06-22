package net.forthecrown.antigrief.commands;

import net.forthecrown.antigrief.ui.AdminUi;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandPunish extends FtcCommand {

  public CommandPunish() {
    super("Punish");

    setDescription("Opens the punishment menu for a specific user");
    setAliases("p");
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /Punish
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.USER)
            .executes(c -> {
              User user = getUserSender(c);
              User target = Arguments.getUser(c, "user");

              AdminUi.open(user, target);
              return 0;
            })
        );
  }
}
package net.forthecrown.commands.marriage;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.UserInteractions;

public class CommandMarriageDeny extends FtcCommand {

  public CommandMarriageDeny() {
    super("marrydeny");

    setDescription("Deny a person's marriage request");
    setAliases("mdeny");
    setPermission(Permissions.MARRY);
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /marriagecancel
   *
   * Permissions used:
   * ftc.marry
   *
   * Main Author: Julie
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          User user = getUserSender(c);
          UserInteractions inter = user.getInteractions();

          if (inter.getLastProposal() == null) {
            throw Exceptions.NO_PROPOSALS;
          }

          User lastRequest = Users.get(inter.getLastProposal());
          inter.setLastProposal(null);

          lastRequest.sendMessage(Messages.proposeDenySender(user));
          user.sendMessage(Messages.proposeDenyTarget(lastRequest));
          return 0;
        });
  }
}
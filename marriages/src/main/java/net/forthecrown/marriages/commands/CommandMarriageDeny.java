package net.forthecrown.marriages.commands;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.marriages.MExceptions;
import net.forthecrown.marriages.MPermissions;
import net.forthecrown.marriages.requests.Proposal;
import net.forthecrown.marriages.requests.Proposals;
import net.forthecrown.user.User;

public class CommandMarriageDeny extends FtcCommand {

  public CommandMarriageDeny() {
    super("marrydeny");

    setDescription("Deny a person's marriage request");
    setAliases("mdeny");
    setPermission(MPermissions.MARRY);
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

          Proposal proposal = Proposals.TABLE.latestIncoming(user);

          if (proposal == null) {
            throw MExceptions.NO_PROPOSALS;
          }

          proposal.deny();
          return 0;
        });
  }
}
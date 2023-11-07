package net.forthecrown.marriages.commands;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.marriages.MExceptions;
import net.forthecrown.marriages.MPermissions;
import net.forthecrown.marriages.requests.Proposal;
import net.forthecrown.marriages.requests.Proposals;
import net.forthecrown.user.User;

public class CommandMarriageAccept extends FtcCommand {

  public CommandMarriageAccept() {
    super("marryaccept");

    setAliases("maccept");
    setPermission(MPermissions.MARRY);
    setDescription("Accept a marriage proposal");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /marryaccept
   * /maccept
   *
   * Permissions used:
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user>", "Accepts a marriage proposal from a <user>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.USER)
            .executes(c -> {
              User proposed = getUserSender(c);
              User proposer = Arguments.getUser(c, "user");

              Proposal proposal = Proposals.TABLE.getIncoming(proposed, proposer);

              if (proposal == null) {
                throw MExceptions.NO_PROPOSALS;
              }

              proposal.accept();
              return 0;
            })
        );
  }
}
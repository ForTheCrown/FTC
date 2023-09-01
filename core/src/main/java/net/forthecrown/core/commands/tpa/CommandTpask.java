package net.forthecrown.core.commands.tpa;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandTpask extends FtcCommand {

  public CommandTpask() {
    super("tpask");

    setAliases("tpa", "tprequest", "tpr", "etpa", "etpask");
    setDescription("Asks a to teleport to a player.");
    setPermission(TpPermissions.TPA);
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Allows players to teleport to another player by asking them.
   *
   * Valid usages of command:
   * - /tpask <player>
   *
   * Permissions used:
   * - ftc.commands.tpa
   *
   * Main Author: Julie
   * Edit by: Wout
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<player>").addInfo("Asks to teleport to a <player>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.then(argument("player", Arguments.ONLINE_USER)
        .executes(c -> {
          User player = getUserSender(c);
          User target = Arguments.getUser(c, "player");

          TeleportRequest.run(player, target, false);
          return 0;
        })
    );
  }
}
package net.forthecrown.commands.tpa;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandTpCancel extends FtcCommand {

  public CommandTpCancel() {
    super("tpcancel");

    setPermission(Permissions.TPA);
    setDescription("Cancels a teleport");
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      User user = getUserSender(c);

      if (user.isTeleporting()) {
        throw Exceptions.NOT_CURRENTLY_TELEPORTING;
      }

      user.getLastTeleport().interrupt();
      return 0;
    });
  }
}
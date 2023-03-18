package net.forthecrown.commands;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.tpa.CommandTpask;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;

public class CommandBack extends FtcCommand {

  public CommandBack() {
    super("back");

    setPermission(Permissions.BACK);
    setAliases("return");
    setDescription("Teleports you to your previous location");
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          User user = getUserSender(c);
          if (!user.checkTeleporting()) {
            return 0;
          }

          if (user.getReturnLocation() == null) {
            throw Exceptions.NO_RETURN;
          }

          if (!user.hasPermission(Permissions.WORLD_BYPASS)
              && CommandTpask.isInvalidWorld(user.getReturnLocation().getWorld())
          ) {
            throw Exceptions.CANNOT_RETURN;
          }

          user.createTeleport(user::getReturnLocation, UserTeleport.Type.BACK)
              .start();
          return 0;
        });
  }
}
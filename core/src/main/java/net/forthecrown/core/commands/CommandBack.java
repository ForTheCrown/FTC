package net.forthecrown.core.commands;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.events.WorldAccessTestEvent;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;
import org.bukkit.Location;

public class CommandBack extends FtcCommand {

  public CommandBack() {
    super("back");

    setPermission(CorePermissions.BACK);
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

          Location ret = user.getReturnLocation();
          if (ret == null) {
            throw CoreExceptions.NO_RETURN;
          }

          WorldAccessTestEvent.testWorldAccess(user.getPlayer(), ret.getWorld()).orThrow(() -> {
            return Text.format("Not allowed to return to world {0}",
                Text.formatWorldName(ret.getWorld())
            );
          });

          user.createTeleport(user::getReturnLocation, UserTeleport.Type.BACK)
              .start();

          return 0;
        });
  }
}
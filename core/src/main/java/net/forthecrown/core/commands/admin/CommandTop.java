package net.forthecrown.core.commands.admin;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;
import org.bukkit.HeightMap;
import org.bukkit.Location;

public class CommandTop extends FtcCommand {

  public CommandTop() {
    super("top");

    setPermission(CorePermissions.CMD_TELEPORT);
    setDescription("Teleports you to the top block in your X and Z pos");

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          User user = getUserSender(c);
          Location top = user.getLocation().toHighestLocation(HeightMap.WORLD_SURFACE);

          user.createTeleport(() -> top, UserTeleport.Type.TELEPORT)
              .setDelay(null)
              .setSetReturn(false)
              .start();

          return 0;
        });
  }
}
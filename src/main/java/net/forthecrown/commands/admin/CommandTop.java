package net.forthecrown.commands.admin;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;
import org.bukkit.HeightMap;
import org.bukkit.Location;

public class CommandTop extends FtcCommand {

  public CommandTop() {
    super("top");

    setPermission(Permissions.ADMIN);
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
              .setDelayed(false)
              .setSetReturn(false)
              .start();
          return 0;
        });
  }
}
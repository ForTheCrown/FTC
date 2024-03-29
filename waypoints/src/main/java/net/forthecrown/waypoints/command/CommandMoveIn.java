package net.forthecrown.waypoints.command;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.waypoints.WExceptions;
import net.forthecrown.waypoints.WMessages;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.Waypoints;
import org.bukkit.Sound;

public class CommandMoveIn extends FtcCommand {

  public CommandMoveIn() {
    super("MoveIn");

    setPermission(WPermissions.WAYPOINTS);
    setDescription("Sets your home waypoint");
    setAliases("sethomepole", "sethomepost");
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /MoveIn
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          User user = getUserSender(c);
          Waypoint waypoint = Waypoints.getNearest(user);

          Waypoints.validateMoveInCooldown(user);

          if (waypoint == null || !waypoint.getBounds().contains(user.getPlayer())) {
            if (waypoint != null) {
              throw WExceptions.farFromWaypoint(waypoint);
            } else {
              throw WExceptions.FAR_FROM_WAYPOINT;
            }
          }

          user.sendMessage(WMessages.HOME_WAYPOINT_SET);
          user.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);

          waypoint.addResident(user.getUniqueId());

          return 0;
        });
  }
}
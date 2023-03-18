package net.forthecrown.commands.waypoint;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.visit.WaypointVisit;

public class CommandHomeWaypoint extends FtcCommand {

  public CommandHomeWaypoint() {
    super("HomeWaypoint");

    setPermission(Permissions.WAYPOINTS);
    setAliases("homepole", "homepost");
    setDescription("Takes you to your home waypoint");
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /HomeWaypoint
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
          Waypoint home = user.getHomes().getHomeTeleport();

          if (home == null) {
            throw Exceptions.NO_HOME_REGION;
          }

          WaypointVisit.visit(user, home);
          return 0;
        });
  }
}
package net.forthecrown.waypoints.command;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.waypoints.WExceptions;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.Waypoints;
import net.forthecrown.waypoints.visit.WaypointVisit;

public class CommandHomeWaypoint extends FtcCommand {

  public CommandHomeWaypoint() {
    super("HomeWaypoint");

    setPermission(WPermissions.WAYPOINTS);
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
          Waypoint home = Waypoints.getHomeWaypoint(user);

          if (home == null) {
            throw WExceptions.NO_HOME_REGION;
          }

          WaypointVisit.visit(user, home);
          return 0;
        });
  }
}
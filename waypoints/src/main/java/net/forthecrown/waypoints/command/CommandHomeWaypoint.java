package net.forthecrown.waypoints.command;

import java.util.UUID;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.waypoints.WExceptions;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
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
          UUID homeId = user.get(Waypoints.HOME_PROPERTY);

          if (homeId.equals(Waypoints.NIL_UUID)) {
            throw WExceptions.NO_HOME_REGION;
          }

          Waypoint home = WaypointManager.getInstance().get(homeId);

          if (home == null) {
            throw WExceptions.NO_HOME_REGION;
          }

          WaypointVisit.visit(user, home);
          return 0;
        });
  }
}
package net.forthecrown.waypoints.command;

import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointConfig;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.visit.WaypointVisit;

public class CommandSpawn extends FtcCommand {

  public CommandSpawn() {
    super("spawn");
    setPermission(WPermissions.WAYPOINTS);
    setDescription("Alias for '/vr Hazelguard'");
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      Cooldown.testAndThrow(c.getSource(), "waypoint_visit", 5 * 20);

      User user = getUserSender(c);

      WaypointManager manager = WaypointManager.getInstance();
      WaypointConfig config = manager.config();
      String spawnWaypoint = config.spawnWaypoint;

      Waypoint waypoint = manager.getExtensive(spawnWaypoint);

      if (waypoint == null) {
        throw Exceptions.create("No spawn region exists! Tell the admins");
      }

      WaypointVisit.visit(user, waypoint);
      return 0;
    });
  }
}

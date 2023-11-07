package net.forthecrown.waypoints.command;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.WExceptions;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoints;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.math.vector.Vector3i;

public class CommandFindPole extends FtcCommand {

  public CommandFindPole() {
    super("findpole");

    setAliases("findpost", "findwaypoint");
    setPermission(WPermissions.WAYPOINTS);
    setDescription("Shows you the nearest waypoint");

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      var user = getUserSender(c);
      var nearest = Waypoints.getNearest(user);

      if (nearest == null) {
        throw WExceptions.FAR_FROM_WAYPOINT;
      }

      Vector3i playerPos = Vectors.intFrom(user.getLocation());
      Vector3i waypointPos = nearest.getPosition();

      double distance = playerPos.distance(waypointPos);

      user.sendMessage(
          Text.format(
              "Nearest waypoint is at &e{0}&6x &e{1}&6y &e{2}&6z "
                  + "&r(&e{3, number, -floor}&r blocks away)",

              NamedTextColor.GRAY,

              waypointPos.x(), waypointPos.y(), waypointPos.z(),
              distance
          )
      );
      return 0;
    });
  }
}

package net.forthecrown.waypoints.type;

import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;

public class AdminWaypoint extends WaypointType {

  public AdminWaypoint() {
    super("Admin");
  }

  @Override
  public Vector3d getVisitPosition(Waypoint waypoint) {
    return waypoint.getPosition()
        .toDouble()
        .add(0.5, 0, 0.5);
  }

  @Override
  public @NotNull Bounds3i createBounds() {
    var size = WaypointManager.getInstance().config().adminWaypointSize;
    return boundsFromSize(size);
  }
}
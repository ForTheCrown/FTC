package net.forthecrown.waypoint.type;

import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.WaypointConfig;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

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
    return boundsFromSize(WaypointConfig.adminWaypointSize);
  }
}
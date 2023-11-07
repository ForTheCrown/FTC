package net.forthecrown.waypoints.type;

import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;

public final class AdminWaypoint extends WaypointType {

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

  @Override
  public int getTopOffset() {
    return -1;
  }

  @Override
  public int getPlatformOffset() {
    return -1;
  }

  @Override
  public ItemStack getDisplayItem(Waypoint waypoint) {
    return new ItemStack(Material.NETHERITE_BLOCK);
  }
}
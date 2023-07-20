package net.forthecrown.waypoints.type;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.Waypoints;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

public final class RegionPoleType extends WaypointType {

  public static final Material[] REGION_POLE_COLUMN = {
      Material.GLOWSTONE,
      Material.GLOWSTONE,
      Material.SEA_LANTERN
  };

  public RegionPoleType() {
    super("Region Pole", REGION_POLE_COLUMN);
  }

  @Override
  public void onPreMove(Waypoint waypoint, Vector3i newPosition, World newWorld) {
    clearPole(waypoint);
  }

  @Override
  public void onDelete(Waypoint waypoint) {
    clearPole(waypoint);
  }

  private void clearPole(Waypoint waypoint) {
    if (!waypoint.get(WaypointProperties.INVULNERABLE)) {
      return;
    }

    var oldBounds = waypoint.getBounds().toWorldBounds(waypoint.getWorld());

    for (var b : oldBounds) {
      b.setType(Material.AIR, false);
    }
  }

  @Override
  public @NotNull Bounds3i createBounds() {
    return boundsFromSize(Waypoints.poleSize());
  }

  @Override
  public void onPostMove(Waypoint waypoint) {
    if (!waypoint.get(WaypointProperties.INVULNERABLE)) {
      return;
    }

    Waypoints.placePole(waypoint);
  }

  @Override
  public Optional<CommandSyntaxException> isValid(Waypoint waypoint) {
    var pos = waypoint.getPosition().add(0, 1, 0);
    return Waypoints.isValidWaypointArea(pos, this, waypoint.getWorld(), false);
  }

  @Override
  public Vector3d getVisitPosition(Waypoint waypoint) {
    return super.getVisitPosition(waypoint).add(0, 1, 0);
  }

  @Override
  public boolean isDestroyed(Waypoint waypoint) {
    return WaypointTypes.isDestroyed(
        getColumn(),
        waypoint.getPosition().add(0, 1, 0),
        waypoint.getWorld()
    );
  }
}
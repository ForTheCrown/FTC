package net.forthecrown.waypoints.type;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.waypoints.Waypoint;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public abstract class WaypointType {

  /**
   * The type's display name, displayed to users through chat and in the dynmap description
   */
  private final String displayName;

  /**
   * Pre waypoint move callback
   */
  public void onPreMove(Waypoint waypoint,
                        Vector3i newPosition,
                        World newWorld
  ) {

  }

  /**
   * Post waypoint move callback
   */
  public void onPostMove(Waypoint waypoint) {

  }

  /**
   * Creates the waypoint's bounding box
   */
  public @NotNull Bounds3i createBounds() {
    return Bounds3i.EMPTY;
  }

  /**
   * Waypoint deletion callback
   */
  public void onDelete(Waypoint waypoint) {

  }

  /**
   * Tests if the waypoint is valid.
   * <p>
   * For any implementations that extend {@link PlayerWaypointType}, this method will simply call
   * <i>that</i> method with the waypoint's data as parameter. For any other implementation, it
   * just
   * returns an empty optional
   *
   * @see net.forthecrown.waypoints.Waypoints#isValidWaypointArea(Vector3i, PlayerWaypointType,
   * World, boolean)
   */
  public Optional<CommandSyntaxException> isValid(Waypoint waypoint) {
    return Optional.empty();
  }

  /**
   * Gets the teleport position to the waypoint
   */
  public Vector3d getVisitPosition(Waypoint waypoint) {
    return waypoint.getBounds()
        .center()
        .withY(waypoint.getBounds().maxY());
  }

  public boolean isDestroyed(Waypoint waypoint) {
    return false;
  }

  protected static Bounds3i boundsFromSize(Vector3i size) {
    Vector3i halfSize = size.div(2, 1, 2);

    return Bounds3i.of(
        halfSize.negate().withY(0),
        halfSize.sub(0, 1, 0)
    );
  }
}
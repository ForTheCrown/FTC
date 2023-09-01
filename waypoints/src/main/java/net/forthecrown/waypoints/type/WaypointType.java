package net.forthecrown.waypoints.type;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import net.forthecrown.user.User;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.waypoints.Waypoint;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

@Getter
public abstract class WaypointType {

  /**
   * The type's display name, displayed to users through chat and in the dynmap description
   */
  private final String displayName;

  private final Material[] column;

  public WaypointType(String displayName) {
    this(displayName, null);
  }

  public WaypointType(String displayName, Material[] column) {
    Objects.requireNonNull(displayName, "Null display name");

    this.displayName = displayName;
    this.column = column;
  }

  /**
   * Pre waypoint move callback
   */
  public void onPreMove(Waypoint waypoint, Vector3i newPosition, World newWorld) {

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

  public void onCreate(User creator, Vector3i topPos) throws CommandSyntaxException {

  }

  public void onPostCreate(Waypoint waypoint, User creator) {

  }

  /**
   * Tests if the waypoint is valid.
   * <p>
   * For any implementations that extend {@link PlayerWaypointType}, this method will simply call
   * <i>that</i> method with the waypoint's data as parameter. For any other implementation, it
   * just
   * returns an empty optional
   *
   * @see net.forthecrown.waypoints.Waypoints#isValidWaypointArea(Vector3i, WaypointType, World,
   * boolean)
   */
  public Optional<CommandSyntaxException> isValid(Waypoint waypoint) {
    return Optional.empty();
  }

  public boolean canBeRemoved(Waypoint waypoint) {
    return true;
  }

  /**
   * Gets the teleport position to the waypoint
   */
  public Vector3d getVisitPosition(Waypoint waypoint) {
    if (column != null) {
      return waypoint.getPosition().toDouble().add(0.5, column.length, 0.5);
    }

    return waypoint.getBounds().center().withY(waypoint.getBounds().maxY());
  }

  public boolean isDestroyed(Waypoint waypoint) {
    return false;
  }

  public static Bounds3i boundsFromSize(Vector3i size) {
    Vector3i halfSize = size.div(2, 1, 2);

    return Bounds3i.of(
        halfSize.negate().withY(0),
        halfSize.sub(0, 1, 0)
    );
  }

  public TextColor getNameColor() {
    return NamedTextColor.GRAY;
  }

  public String getEffectiveName(Waypoint waypoint) {
    return null;
  }

  public final boolean isBuildable() {
    return internalIsBuildable() && column != null;
  }

  protected boolean internalIsBuildable() {
    return false;
  }
}
package net.forthecrown.waypoints.type;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.WaypointPrefs;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.Waypoints;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

public class PlayerWaypointType extends WaypointType {

  public static final Material[] PLAYER_COLUMN = {
      Material.STONE_BRICKS,
      Material.STONE_BRICKS,
      Material.CHISELED_STONE_BRICKS,
  };

  public PlayerWaypointType() {
    super("Player-Made", PLAYER_COLUMN);
  }

  public PlayerWaypointType(String displayName, Material[] column) {
    super(displayName, column);
  }

  @Override
  public void onCreate(User creator, Vector3i topPos) throws CommandSyntaxException {
    Waypoints.validateMoveInCooldown(creator);
  }

  @Override
  public void onPostCreate(Waypoint waypoint, User creator) {
    if (waypoint.get(WaypointProperties.OWNER) == null) {
      waypoint.set(WaypointProperties.OWNER, creator.getUniqueId());
    }

    creator.setTimeToNow(TimeField.LAST_MOVEIN);
    creator.set(WaypointPrefs.HOME_PROPERTY, waypoint.getId());
  }

  @Override
  public @NotNull Bounds3i createBounds() {
    var config = WaypointManager.getInstance().config();
    return boundsFromSize(config.playerWaypointSize);
  }

  @Override
  public Optional<CommandSyntaxException> isValid(Waypoint waypoint) {
    return Waypoints.isValidWaypointArea(
        waypoint.getPosition(),
        this,
        waypoint.getWorld(),
        false
    );
  }

  @Override
  public Vector3d getVisitPosition(Waypoint waypoint) {
    return waypoint.getPosition()
        .toDouble()
        .add(0.5, getColumn().length, 0.5);
  }

  @Override
  public boolean isDestroyed(Waypoint waypoint) {
    return WaypointTypes.isDestroyed(getColumn(), waypoint.getPosition(), waypoint.getWorld());
  }

  @Override
  protected boolean internalIsBuildable() {
    return true;
  }
}
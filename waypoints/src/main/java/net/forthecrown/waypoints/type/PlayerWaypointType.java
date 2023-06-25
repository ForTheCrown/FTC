package net.forthecrown.waypoints.type;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.Waypoints;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

@Getter
public class PlayerWaypointType extends WaypointType {

  private final Material[] column;

  @Setter
  private WaypointFactory factory;

  public PlayerWaypointType(String displayName, Material[] column) {
    super(displayName);
    this.column = column;
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
        .add(0.5, column.length, 0.5);
  }

  @Override
  public boolean isDestroyed(Waypoint waypoint) {
    return isDestroyed(waypoint.getPosition(), waypoint.getWorld());
  }

  protected boolean isDestroyed(Vector3i pos, World world) {
    int destroyedCount = 0;

    for (int i = 0; i < column.length; i++) {
      var bPos = i == 0 ? pos : pos.add(0, i, 0);
      var block = Vectors.getBlock(bPos, world);

      if (block.getType() != column[i]) {
        ++destroyedCount;
      }
    }

    return destroyedCount >= column.length;
  }
}
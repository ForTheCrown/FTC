package net.forthecrown.waypoints.type;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.Waypoints;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
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
  public TextColor getNameColor(Waypoint waypoint) {
    return NamedTextColor.YELLOW;
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

    waypoint.breakColumn();
  }

  @Override
  public @NotNull Bounds3i createBounds() {
    return boundsFromSize(WaypointManager.getInstance().config().playerWaypointSize);
  }

  @Override
  public void onPostMove(Waypoint waypoint) {
    if (!waypoint.get(WaypointProperties.INVULNERABLE)) {
      return;
    }

    waypoint.placeColumn();
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

  @Override
  public int getTopOffset() {
    return getColumn().length;
  }

  @Override
  public int getPlatformOffset() {
    return 0;
  }

  @Override
  public ItemStack getDisplayItem(Waypoint waypoint) {
    return new ItemStack(Material.GLOWSTONE);
  }
}
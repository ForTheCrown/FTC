package net.forthecrown.waypoints;

import java.time.Duration;
import net.forthecrown.Worlds;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.World;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.math.vector.Vector3i;

@ConfigSerializable
public class WaypointConfig {

  /**
   * The required X Y Z size of player-made waypoints
   */
  public Vector3i
      playerWaypointSize = Vector3i.from(5),
      adminWaypointSize = Vector3i.from(5);

  /**
   * Name of the spawn waypoint
   */
  public String spawnWaypoint = "Hazelguard";

  /**
   * Worlds players cannot move their waypoints to
   */
  public String[] disabledPlayerWorlds = {"world_void", "world_resource", "world_the_end"};

  public boolean allowEndWaypoints = true;

  public boolean hulkSmashPoles = true;

  /**
   * Delay between a waypoint being marked for removal and when it's actually deleted
   */
  public Duration waypointDeletionDelay = Duration.ofDays(7);

  public Duration moveInCooldown = Duration.ofHours(1);

  public Duration validInviteTime = Duration.ofMinutes(10);

  Duration autoSaveInterval = Duration.ofMinutes(30);

  /**
   * Determines whether /movein enforces a cooldown
   */
  public boolean moveInHasCooldown = false;

  public boolean isDisabledWorld(World w) {
    if (Worlds.end().equals(w)) {
      return allowEndWaypoints;
    }

    return ArrayUtils.contains(disabledPlayerWorlds, w.getName());
  }
}
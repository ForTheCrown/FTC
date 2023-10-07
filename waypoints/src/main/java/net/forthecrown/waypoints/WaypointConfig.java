package net.forthecrown.waypoints;

import java.time.Duration;
import net.forthecrown.Worlds;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.World;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.math.vector.Vector3i;

@ConfigSerializable
public class WaypointConfig {

  public Vector3i playerWaypointSize = Vector3i.from(5);
  public Vector3i adminWaypointSize = Vector3i.from(5);
  public String spawnWaypoint = "Hazelguard";

  public String[] disabledPlayerWorlds = {"world_void", "world_resource", "world_the_end"};

  public boolean allowEndWaypoints = true;
  public boolean hulkSmashPoles = true;

  public Duration waypointDeletionDelay = Duration.ofDays(7);
  public Duration moveInCooldown = Duration.ZERO;
  public Duration validInviteTime = Duration.ofMinutes(10);

  Duration autoSaveInterval = Duration.ofMinutes(30);

  public boolean isDisabledWorld(World w) {
    if (Worlds.end().equals(w)) {
      return allowEndWaypoints;
    }

    return ArrayUtils.contains(disabledPlayerWorlds, w.getName());
  }
}
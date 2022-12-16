package net.forthecrown.waypoint;

import lombok.experimental.UtilityClass;
import net.forthecrown.core.config.ConfigData;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3i;

import java.util.concurrent.TimeUnit;

@ConfigData(filePath = "waypoints.json")
public @UtilityClass class WaypointConfig {
    /** The required X Y Z size of player-made waypoints */
    public Vector3i
            playerWaypointSize      = Vector3i.from(5);

    /** Name of the spawn waypoint */
    public String
            spawnWaypoint           = "Hazelguard";

    /** Worlds players cannot move their waypoints to */
    public String[]
            disabledPlayerWorlds    = { "world_void", "world_resource", "world_the_end" };

    /**
     * Delay between a waypoint being marked for
     * removal and when it's actually deleted
     */
    public long
            waypointDeletionDelay   = TimeUnit.DAYS.toMillis(7),
            moveInCooldown          = TimeUnit.HOURS.toMillis(1);

    /** Determines whether /movein enforces a cooldown */
    public boolean
            moveInHasCooldown       = false;

    public boolean isDisabledWorld(World w) {
        return ArrayUtils.contains(disabledPlayerWorlds, w.getName());
    }
}
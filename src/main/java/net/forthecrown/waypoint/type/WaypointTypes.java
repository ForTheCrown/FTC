package net.forthecrown.waypoint.type;

import lombok.experimental.UtilityClass;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.waypoint.Waypoints;

public @UtilityClass class WaypointTypes {
    public final Registry<WaypointType> REGISTRY = Registries.newFreezable();

    public final AdminWaypoint
            ADMIN       = register("admin", new AdminWaypoint());

    public final PlayerWaypointType
            GUILD       = register("guild", new PlayerWaypointType("Guild Waypoint", Waypoints.GUILD_COLUMN));

    public final PlayerWaypointType
            PLAYER      = register("player", new PlayerWaypointType("Player-Made", Waypoints.PLAYER_COLUMN));

    public final RegionPoleType
            REGION_POLE = register("region_pole", new RegionPoleType());

    // Called reflectively by BootStrap
    @OnEnable
    private static void init() {
        REGISTRY.freeze();
    }

    private static <T extends WaypointType> T register(String key, T type) {
        return (T) REGISTRY.register(key, type).getValue();
    }
}
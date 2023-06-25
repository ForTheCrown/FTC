package net.forthecrown.waypoints.type;

import static net.forthecrown.waypoints.Waypoints.validateMoveInCooldown;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.experimental.UtilityClass;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.Waypoints;

public @UtilityClass class WaypointTypes {

  public final Registry<WaypointType> REGISTRY = Registries.newFreezable();

  public final AdminWaypoint ADMIN
      = register("admin", new AdminWaypoint());

  public final PlayerWaypointType GUILD
      = register("guild", new PlayerWaypointType("Guild", Waypoints.GUILD_COLUMN));

  public final PlayerWaypointType PLAYER
      = register("player", new PlayerWaypointType("Player-Made", Waypoints.PLAYER_COLUMN));

  public final RegionPoleType REGION_POLE
      = register("region_pole", new RegionPoleType());

  static {
    REGISTRY.freeze();

    PLAYER.setFactory(new WaypointFactory() {
      @Override
      public void onCreate(User user) throws CommandSyntaxException {
        validateMoveInCooldown(user);
      }

      @Override
      public void postCreate(Waypoint waypoint, User user) {
        if (waypoint.get(WaypointProperties.OWNER) == null) {
          waypoint.set(
              WaypointProperties.OWNER,
              user.getUniqueId()
          );
        }

        user.setTimeToNow(TimeField.LAST_MOVEIN);
        user.set(Waypoints.HOME_PROPERTY, waypoint.getId());
      }
    });
  }

  private static <T extends WaypointType> T register(String key, T type) {
    return (T) REGISTRY.register(key, type).getValue();
  }
}
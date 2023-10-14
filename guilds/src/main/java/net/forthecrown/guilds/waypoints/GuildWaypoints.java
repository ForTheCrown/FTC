package net.forthecrown.guilds.waypoints;

import java.util.UUID;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.utils.io.FtcCodecs;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.WaypointProperty;
import net.forthecrown.waypoints.type.WaypointTypes;

public final class GuildWaypoints {
  private GuildWaypoints() {}

  public static final WaypointProperty<UUID> GUILD_OWNER
      = new WaypointProperty<>("guild_owner", ArgumentTypes.uuid(), FtcCodecs.INT_ARRAY_UUID, null);

  public static final GuildWaypointType GUILD_TYPE = new GuildWaypointType();

  public static void init(GuildManager manager) {
    WaypointManager waypoints = WaypointManager.getInstance();
    waypoints.addExtension("guilds", new GuildWaypointExtension(manager));
    WaypointTypes.REGISTRY.register("guild", GUILD_TYPE);
  }

  public static void close() {
    WaypointManager waypoints = WaypointManager.getInstance();
    waypoints.removeExtension("guilds");
  }
}

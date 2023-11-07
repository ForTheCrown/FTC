package net.forthecrown.waypoints;

import static net.forthecrown.command.Exceptions.create;
import static net.forthecrown.command.Exceptions.format;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.user.User;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.type.WaypointType;
import net.forthecrown.waypoints.type.WaypointTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.spongepowered.math.vector.Vector3i;

public interface WExceptions {

  CommandSyntaxException NO_HOME_REGION = create("You do not have a home waypoint");

  CommandSyntaxException WAYPOINTS_WRONG_WORLD = create("Waypoints are disabled in this world!");

  CommandSyntaxException ONLY_IN_VEHICLE = create("Can only teleport in a vehicle");

  CommandSyntaxException FAR_FROM_WAYPOINT = create(
      "Too far from any waypoint, or in a world without waypoints"
  );

  CommandSyntaxException UNLOADED_WORLD = create("This waypoint is in an unloaded world!");

  CommandSyntaxException FACE_WAYPOINT
      = create("You must be looking at a waypoint's pillar blocks");

  static CommandSyntaxException unknownRegion(StringReader reader, int cursor) {
    return format(
        "There's no region, player or guild named '{0}'.\nUse /listregions to list all regions.",
        reader.getString().substring(cursor, reader.getCursor())
    );
  }

  /**
   * Creates an exception stating the given region name is unknown
   *
   * @param name The region's name
   * @return The created exception
   */
  static CommandSyntaxException unknownRegion(String name) {
    return format("Unknown region: '{0}'", name);
  }

  static CommandSyntaxException farFromWaypoint(Waypoint waypoint) {
    var pos = waypoint.getPosition();
    return farFromWaypoint(pos.x(), pos.y(), pos.z());
  }

  static CommandSyntaxException farFromWaypoint(int x, int y, int z) {
    return format("Too far from a waypoint.\nClosest pole is at {0, vector}",
        Vector3i.from(x, y, z)
    );
  }

  static CommandSyntaxException privateRegion(Waypoint region) {
    return format("'{0}' is a private waypoint", region.get(WaypointProperties.NAME));
  }

  static CommandSyntaxException brokenWaypoint(Vector3i pos, Material found, Material expected) {
    return format("Waypoint is broken at {0}! Expected {1}, found {2}", pos, expected, found);
  }

  static CommandSyntaxException invalidWaypointTop(Material m) {
    Component tops = TextJoiner.onComma()
        .add(
            WaypointTypes.REGISTRY.stream()
                .map(Holder::getValue)
                .filter(WaypointType::isBuildable)
                .map(type -> {
                  Material[] col = type.getColumn();
                  Material top = col[col.length - 1];

                  return Text.format("{0} ({1} waypoint)",
                      top, type.getDisplayName().toLowerCase().replace("-made", "")
                  );
                })
        )
        .asComponent();

    return format("{0} is an invalid waypoint top block! Must be one of: {1}", m, tops);
  }

  static CommandSyntaxException waypointBlockNotEmpty(Block pos) {
    var areaSize = playerWaypointSize();

    return format(
        "Waypoint requires a clear {1}x{2}x{3} area around it!\n"
            + "Non-empty block found at {0, vector}",

        Vectors.from(pos),
        areaSize.x(), areaSize.y(), areaSize.z()
    );
  }

  static CommandSyntaxException snowTooHigh(Block block) {
    var areaSize = playerWaypointSize();
    var pos = Vectors.from(block);

    return format("Waypoint requires a clear {1}x{2}x{3} area around it!\n" +
        "Snow block higher than half a block found at {0, vector}",
        pos,
        areaSize.x(), areaSize.y(), areaSize.z()
    );
  }

  static CommandSyntaxException overlappingWaypoints(int overlapping) {
    return format("This waypoint is overlapping {0, number} other waypoint(s)", overlapping);
  }

  static CommandSyntaxException waypointPlatform() {
    var size = playerWaypointSize();
    return waypointPlatform(size);
  }

  static CommandSyntaxException waypointPlatform(Vector3i size) {
    return format("Waypoint requires a {0}x{1} platform under it!", size.x(), size.z());
  }

  private static Vector3i playerWaypointSize() {
    return WaypointManager.getInstance().config().playerWaypointSize;
  }


  static CommandSyntaxException notInvited(User user) {
    return format("{0, user} has not invited you.", user);
  }

  static CommandSyntaxException noHomeWaypoint(User user) {
    return format("{0, user} does not have a home waypoint", user);
  }

  static CommandSyntaxException nonReplaceableFloorBlock(Block block) {
    Vector3i vec = Vectors.from(block);
    return format("{0} at {1, vector} cannot be replaced to create a waypoint platform",
        block.getType(), vec
    );
  }

  static CommandSyntaxException waypointAlreadySet(
      Waypoint existing,
      String messagePrefix,
      String waypointPrefix
  ) {
    Component how = text(
        """
        Right-Click the 'edit waypoint'
        sign on the waypoint you want to delete
        and select the delete option
        """.trim()
    );

    var p = existing.getPosition();
    Location location = new Location(existing.getWorld(), p.x(), p.y(), p.z());

    return format(
        "{2}, remove the old one before making a new one {0}\n"
            + "Your current {3}-waypoint is at {1, location}",

        text("[How?]", NamedTextColor.AQUA)
            .hoverEvent(how),

        location,

        messagePrefix,
        waypointPrefix
    );
  }

  static CommandSyntaxException homeAlreadySet(Waypoint currentHome) {
    return waypointAlreadySet(currentHome, "You already have a home waypoint", "home");
  }

  static CommandSyntaxException creationDisabled() {
    return create("Waypoint creation is disabled here");
  }
}
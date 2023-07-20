package net.forthecrown.waypoints;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.serialization.Codec.BOOL;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.Codec;
import java.util.UUID;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.utils.io.FtcCodecs;
import net.forthecrown.waypoints.type.PlayerWaypointType;
import net.forthecrown.waypoints.type.WaypointTypes;

public class WaypointProperties {

  /**
   * Registry of waypoint properties
   */
  public static final Registry<WaypointProperty> REGISTRY = Registries.newFreezable();

  /**
   * Determines if a pole can be destroyed, if set to true, a waypoint will also never be
   * automatically deleted
   */
  public static final WaypointProperty<Boolean> INVULNERABLE
      = new WaypointProperty<>("invulnerable", bool(), BOOL, false);

  /**
   * Determines if the waypoint can be visited by others without invitation, only applies to named
   * regions that wish to still require invitation to visit.
   */
  public static final WaypointProperty<Boolean> PUBLIC
      = new WaypointProperty<>("public", bool(), BOOL, true);

  /**
   * Only applies to named waypoints to determine whether they want to have or disallow the dynmap
   * marker
   */
  public static final WaypointProperty<Boolean> ALLOWS_MARKER
      = new WaypointProperty<>("allowsMarker", bool(), BOOL, true);

  /**
   * Only applies to named regions, sets the waypoint's marker icon to be the donator icon instead
   * of the normal icon.
   */
  public static final WaypointProperty<Boolean> SPECIAL_MARKER
      = new WaypointProperty<>("specialMarker", bool(), BOOL, false);

  /**
   * Property only used for region poles to determine whether they should display their resident
   * count on the pole.
   */
  public static final WaypointProperty<Boolean> HIDE_RESIDENTS
      = new WaypointProperty<>("hideResidents", bool(), BOOL, false)
      .setCallback((waypoint, oldValue, value) -> {
        if (waypoint.getType() != WaypointTypes.REGION_POLE || !waypoint.get(INVULNERABLE)) {
          return;
        }

        Waypoints.placePole(waypoint);
      });

  /**
   * The region's name, will be used on the dynmap, if they allow for the marker, and can be used by
   * other players to visit this region with '/visit [name]'
   */
  public static final WaypointProperty<String> NAME
      = new WaypointProperty<>("name", StringArgumentType.string(), Codec.STRING, null)
      .setCallback((waypoint, oldValue, value) -> {
        WaypointManager.getInstance().onRename(waypoint, oldValue, value);

        if (waypoint.getType() == WaypointTypes.REGION_POLE && waypoint.get(INVULNERABLE)) {
          Waypoints.placePole(waypoint);
        } else if (waypoint.getType() instanceof PlayerWaypointType) {
          Waypoints.setNameSign(
              waypoint,
              waypoint.getEffectiveName()
          );
        }
      })

      .setValidator((waypoint, newValue) -> {
        if (Waypoints.isValidName(newValue)) {
          return;
        }

        throw Exceptions.format("Invalid waypoint name '{0}'", newValue);
      });

  /**
   * The UUID of the player that owns the waypoint
   */
  public static final WaypointProperty<UUID> OWNER
      = new WaypointProperty<>("owner", ArgumentTypes.uuid(), FtcCodecs.INT_ARRAY_UUID, null);
}
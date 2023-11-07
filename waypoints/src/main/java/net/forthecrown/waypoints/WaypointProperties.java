package net.forthecrown.waypoints;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.serialization.Codec.BOOL;
import static com.mojang.serialization.Codec.INT;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.UUID;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.utils.io.FtcCodecs;
import org.bukkit.inventory.ItemStack;

public class WaypointProperties {

  /**
   * Registry of waypoint properties
   */
  public static final Registry<WaypointProperty> REGISTRY = Registries.newFreezable();

  static final Map<String, String> RENAMES = Map.ofEntries(
      Map.entry("allowsMarker", "allows_marker"),
      Map.entry("specialMarker", "special_marker"),
      Map.entry("hideResidents", "hide_residents"),
      Map.entry("guildOwner", "guild_owner")
  );

  /**
   * Determines if a pole can be destroyed, if set to true, a waypoint will also never be
   * automatically deleted
   */
  public static final WaypointProperty<Boolean> INVULNERABLE
      = new WaypointProperty<>("invulnerable", bool(), BOOL, false)
      .setUpdatesMarker(false);

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
      = new WaypointProperty<>("allows_marker", bool(), BOOL, true);

  /**
   * Only applies to named regions, sets the waypoint's marker icon to be the donator icon instead
   * of the normal icon.
   */
  public static final WaypointProperty<Boolean> SPECIAL_MARKER
      = new WaypointProperty<>("special_marker", bool(), BOOL, false);

  public static final WaypointProperty<Integer> VISITS_DAILY
      = new WaypointProperty<>("visits/daily", integer(), INT, 0);

  public static final WaypointProperty<Integer> VISITS_MONTHLY
      = new WaypointProperty<>("visits/monthly", integer(), INT, 0);

  public static final WaypointProperty<Integer> VISITS_TOTAL
      = new WaypointProperty<>("visits/total", integer(), INT, 0);

  public static final WaypointProperty<ItemStack> DISPLAY_ITEM
      = new WaypointProperty<>("display_material", Arguments.ITEMSTACK, FtcCodecs.ITEM_CODEC, null);

  public static final WaypointProperty<Float> VISIT_YAW
      = new WaypointProperty<>("visit_rotation/yaw", floatArg(-180, 180), Codec.FLOAT, null);

  public static final WaypointProperty<Float> VISIT_PITCH
      = new WaypointProperty<>("visit_rotation/pitch", floatArg(-90, 90), Codec.FLOAT, null);

  /**
   * Property only used for region poles to determine whether they should display their resident
   * count on the pole.
   */
  public static final WaypointProperty<Boolean> HIDE_RESIDENTS
      = new WaypointProperty<>("hide_residents", bool(), BOOL, false)
      .setUpdatesMarker(false)
      .setCallback((waypoint, oldValue, value) -> {
        waypoint.updateResidentsSign();
      });

  /**
   * The region's name, will be used on the dynmap, if they allow for the marker, and can be used by
   * other players to visit this region with '/visit [name]'
   */
  public static final WaypointProperty<String> NAME
      = new WaypointProperty<>("name", StringArgumentType.string(), Codec.STRING, null)
      .setCallback((waypoint, oldValue, value) -> {
        WaypointManager.getInstance().onRename(waypoint, oldValue, value);
        waypoint.updateNameSign();
      })

      .setValidator((waypoint, newValue) -> {
        DataResult<String> result = Waypoints.validateWaypointName(newValue);

        if (result.error().isEmpty()) {
          return;
        }

        throw Exceptions.format("Invalid waypoint name '{0}': {1}",
            newValue, result.error().get().message()
        );
      });

  /**
   * The UUID of the player that owns the waypoint
   */
  public static final WaypointProperty<UUID> OWNER
      = new WaypointProperty<>("owner", ArgumentTypes.uuid(), FtcCodecs.INT_ARRAY_UUID, null);
}
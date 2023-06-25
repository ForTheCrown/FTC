package net.forthecrown.waypoints;

import com.google.common.base.Strings;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

/**
 * Methods relating to Waypoints and their Dynmap markers.
 */
@UtilityClass
class WaypointDynmap {

  /**
   * Waypoint Marker set ID
   */
  public static final String SET_ID = "waypoint_marker_set";

  /**
   * Waypoint marker set display name
   */
  public static final String SET_NAME = "Waypoints";

  public static final String NORMAL_LABEL = "region_pole_normal";

  public static final String SPECIAL_LABEL = "region_pole_special";

  /**
   * Updates the marker of the given waypoint.
   * <p>
   * If the waypoint doesn't have a name, the marker is deleted, if it exists.
   * <p>
   * If the marker should exist, but doesn't, it's created, if it does exist, it's data is updated
   * to be in sync with the actual waypoint
   */
  static void updateMarker(Waypoint waypoint) {
    var set = getSet();
    var name = waypoint.getEffectiveName();
    var marker = set.findMarker(waypoint.getMarkerId());

    if (Strings.isNullOrEmpty(name) || !waypoint.get(WaypointProperties.ALLOWS_MARKER)) {
      if (marker != null) {
        marker.deleteMarker();
      }

      return;
    }

    MarkerIcon icon = waypoint.get(WaypointProperties.SPECIAL_MARKER)
        ? getSpecialIcon()
        : getNormalIcon();

    String world = waypoint.getWorld().getName();
    int x = waypoint.getPosition().x();
    int y = waypoint.getPosition().z();
    int z = waypoint.getPosition().y();

    if (marker == null) {
      marker = set.createMarker(
          waypoint.getMarkerId(),
          waypoint.get(WaypointProperties.NAME),

          // Location
          world, x, y, z,

          // Icon
          icon,

          // Persistent
          true
      );
    } else {
      marker.setLabel(name);
      marker.setMarkerIcon(icon);
      marker.setLocation(world, x, y, z);
    }

    marker.setDescription(name);
  }

  static void removeMarker(Waypoint waypoint) {
    var marker = getMarker(waypoint);

    if (marker == null) {
      return;
    }

    marker.deleteMarker();
  }

  static Marker getMarker(Waypoint waypoint) {
    return getSet().findMarker(waypoint.getMarkerId());
  }

  static MarkerAPI getMarkerApi() {
    DynmapCommonAPI api = (DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("Dynmap");
    return api.getMarkerAPI();
  }

  static MarkerSet getSet() {
    MarkerAPI api = getMarkerApi();

    return Objects.requireNonNullElseGet(
        api.getMarkerSet(SET_ID),
        () -> api.createMarkerSet(SET_ID, SET_NAME, null, true)
    );
  }

  static MarkerIcon getIcon(String id) {
    var api = getMarkerApi();
    MarkerIcon result = api.getMarkerIcon(id);
    var plugin = JavaPlugin.getPlugin(WaypointsPlugin.class);

    return result != null
        ? result
        : api.createMarkerIcon(id, id, plugin.getResource(id + ".png"));
  }

  static MarkerIcon getNormalIcon() {
    return getIcon(NORMAL_LABEL);
  }

  static MarkerIcon getSpecialIcon() {
    return getIcon(SPECIAL_LABEL);
  }
}
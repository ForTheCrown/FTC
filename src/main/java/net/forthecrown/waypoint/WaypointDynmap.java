package net.forthecrown.waypoint;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.DynmapUtil;
import net.forthecrown.core.FtcDynmap;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.Objects;

/**
 * Methods relating to Waypoints and their
 * Dynmap markers.
 * <p>
 * This class should only ever be loaded if
 * dynmap is installed on the server, this can
 * be tested with {@link DynmapUtil#isInstalled()}
 */
@UtilityClass class WaypointDynmap {
    /** Waypoint Marker set ID */
    public static final String SET_ID = "waypoint_marker_set";

    /** Waypoint marker set display name */
    public static final String SET_NAME = "Waypoints";

    /**
     * Updates the marker of the given waypoint.
     * <p>
     * If the waypoint doesn't have a name, the marker
     * is deleted, if it exists.
     * <p>
     * If the marker should exist, but doesn't, it's
     * created, if it does exist, it's data is updated
     * to be in sync with the actual waypoint
     */
    static void updateMarker(Waypoint waypoint) {
        var set = getSet();
        var name = Waypoints.getEffectiveName(waypoint);
        var marker = set.findMarker(waypoint.getMarkerId());

        if (Strings.isNullOrEmpty(name)
                || !waypoint.get(WaypointProperties.ALLOWS_MARKER)
        ) {
            if (marker != null) {
                marker.deleteMarker();
            }

            return;
        }

        MarkerIcon icon = waypoint.get(WaypointProperties.SPECIAL_MARKER)
                ? FtcDynmap.getSpecialIcon()
                : FtcDynmap.getNormalIcon();

        if (marker == null) {
            marker = set.createMarker(
                    waypoint.getMarkerId(),
                    waypoint.get(WaypointProperties.NAME),

                    // Location
                    waypoint.getWorld().getName(),
                    waypoint.getPosition().x(),
                    waypoint.getPosition().y(),
                    waypoint.getPosition().z(),

                    // Icon
                    icon,

                    // Persistent
                    true
            );
        } else {
            marker.setLabel(name);
            marker.setMarkerIcon(icon);

            marker.setLocation(
                    waypoint.getWorld().getName(),
                    waypoint.getPosition().x(),
                    waypoint.getPosition().y(),
                    waypoint.getPosition().z()
            );
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

    static MarkerSet getSet() {
        var api = FtcDynmap.getMarkerAPI();

        return Objects.requireNonNullElseGet(
                api.getMarkerSet(SET_ID),
                () -> api.createMarkerSet(SET_ID, SET_NAME, null, true)
        );
    }
}
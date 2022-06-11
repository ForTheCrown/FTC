package net.forthecrown.core;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionProperty;
import net.forthecrown.regions.RegionUtil;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

/**
 * A class for interacting with Dynmap easily
 */
public class FtcDynmap extends DynmapCommonAPIListener {
    private static DynmapCommonAPI api;

    public static final String
            NORMAL_LABEL    = "region_pole_normal",
            SPECIAL_LABEL   = "region_pole_special",
            SET_NAME        = "region_poles",
            SET_LABEL       = "Region Poles";

    private static boolean enabled;

    @Override
    public void apiEnabled(DynmapCommonAPI api) {
        FtcDynmap.api = api;
        enabled = true;

        getRegionPoleSet().setHideByDefault(false);
        getRegionPoleSet().setLabelShow(true);
    }

    @Override
    public void apiDisabled(DynmapCommonAPI api) {
        enabled = false;
        FtcDynmap.api = null;
    }

    /**
     * Gets or creates an icon with the given ID
     * @param id The ID to use
     * @return The gotten or created icon
     */
    static MarkerIcon getOrCreate(String id) {
        MarkerIcon result = getMarkerAPI().getMarkerIcon(id);

        return result != null ? result : getMarkerAPI().createMarkerIcon(id, id, Crown.resource(id + ".png"));
    }

    /**
     * Gets the Dynmap API
     * @return The Dynmap API
     */
    public static DynmapCommonAPI getDynmap() {
        return api;
    }

    /**
     * Gets the Marker API
     * @return The Marker API
     */
    public static MarkerAPI getMarkerAPI() {
        return getDynmap().getMarkerAPI();
    }

    /**
     * Gets the normal pole icon
     * @return The Normal pole icon
     */
    public static MarkerIcon getNormalIcon() {
        return getOrCreate(NORMAL_LABEL);
    }

    /**
     * Gets the special pole icon
     * @return The special pole icon
     */
    public static MarkerIcon getSpecialIcon() {
        return getOrCreate(SPECIAL_LABEL);
    }

    /**
     * Checks if Dynmap is currently enabled
     * @return True, if dynmap is enabled, false otherwise
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the region pole marker set
     * @return The region pole marker set
     */
    public static MarkerSet getRegionPoleSet() {
        MarkerSet set = _getRegionPoleSet();

        set.setMarkerSetLabel(SET_LABEL);

        return set;
    }

    private static MarkerSet _getRegionPoleSet() {
        MarkerSet set = getMarkerAPI().getMarkerSet(SET_NAME);
        return set == null ? getMarkerAPI().createMarkerSet(SET_NAME, SET_LABEL, null, true) : set;
    }

    /**
     * Gets the given region's Marker
     * @param data The region to get the marker of
     * @return The gotten marker, null, if the region has no marker
     */
    public static Marker getMarker(PopulationRegion data) {
        return getRegionPoleSet().findMarker(data.getMarkerID());
    }

    /**
     * Creates a marker for the given region
     * @param data The region to create a marker for
     * @return The created marker
     */
    public static Marker createMarker(PopulationRegion data) {
        BlockVector2 vec2 = data.getPolePosition();

        return getRegionPoleSet().createMarker(
                data.getMarkerID(), data.getName(), FtcVars.getRegionWorld().getName(),
                vec2.getX() + 0.5D, RegionUtil.getPoleTop(data), vec2.getZ() + 0.5D,
                data.hasProperty(RegionProperty.PAID_REGION) ? getSpecialIcon() : getNormalIcon(),
                true
        );
    }
}
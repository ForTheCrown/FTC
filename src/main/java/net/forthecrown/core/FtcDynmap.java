package net.forthecrown.core;

import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;

/**
 * A class for interacting with Dynmap easily
 */
public class FtcDynmap {
    public static final String
            NORMAL_LABEL    = "region_pole_normal",
            SPECIAL_LABEL   = "region_pole_special";

    /**
     * Gets or creates an icon with the given ID
     * @param id The ID to use
     * @return The gotten or created icon
     */
    static MarkerIcon getOrCreate(String id) {
        MarkerIcon result = getMarkerAPI().getMarkerIcon(id);
        return result != null ? result : getMarkerAPI().createMarkerIcon(id, id, FTC.getPlugin().getResource(id + ".png"));
    }

    /**
     * Gets the Dynmap API
     * @return The Dynmap API
     */
    public static DynmapCommonAPI getDynmap() {
        return FtcDynmapListener.dynmap;
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

    static void registerListener() {
        DynmapCommonAPIListener.register(new FtcDynmapListener());
    }

    private static class FtcDynmapListener extends DynmapCommonAPIListener {
        private static DynmapCommonAPI dynmap;

        @Override
        public void apiEnabled(DynmapCommonAPI api) {
            dynmap = api;
        }

        @Override
        public void apiDisabled(DynmapCommonAPI api) {
            dynmap = null;
        }
    }
}
package net.forthecrown.core;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.regions.RegionData;
import net.forthecrown.regions.RegionProperty;
import net.forthecrown.regions.RegionUtil;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

public class FtcDynmap extends DynmapCommonAPIListener {
    private static DynmapCommonAPI api;

    public static final String
            NORMAL_LABEL    = "region_pole_normal",
            SPECIAL_LABEL   = "region_pole_special",
            SET_NAME        = "region_poles";

    private static boolean enabled;

    @Override
    public void apiEnabled(DynmapCommonAPI api) {
        FtcDynmap.api = api;
        enabled = true;
    }

    @Override
    public void apiDisabled(DynmapCommonAPI api) {
        enabled = false;
        FtcDynmap.api = null;
    }

    static MarkerIcon getOrCreate(String id) {
        MarkerIcon result = getMarkerAPI().getMarkerIcon(id);

        return result != null ? result : getMarkerAPI().createMarkerIcon(id, id, Crown.resource(id + ".png"));
    }

    public static DynmapCommonAPI getDynmap() {
        return api;
    }

    public static MarkerAPI getMarkerAPI() {
        return getDynmap().getMarkerAPI();
    }

    public static MarkerIcon getNormalIcon() {
        return getOrCreate(NORMAL_LABEL);
    }

    public static MarkerIcon getSpecialIcon() {
        return getOrCreate(SPECIAL_LABEL);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static MarkerSet getRegionPoleSet() {
        MarkerSet set = getMarkerAPI().getMarkerSet(SET_NAME);
        return set == null ? getMarkerAPI().createMarkerSet(SET_NAME, SET_NAME, null, true) : set;
    }

    public static Marker findMarker(RegionData data) {
        Marker marker = getRegionPoleSet().findMarker(data.getMarkerID());

        BlockVector2 vec2 = data.getPolePosition();

        return marker != null ? marker : getRegionPoleSet().createMarker(
                data.getPos().toString(), data.getName(), ComVars.getRegionWorld().getName(),
                vec2.getX() + 0.5D, RegionUtil.getPoleTop(data), vec2.getZ() + 0.5D,
                data.hasProperty(RegionProperty.PAID_REGION) ? getSpecialIcon() : getNormalIcon(),
                true
        );
    }
}

package net.forthecrown.regions;

import net.forthecrown.core.FtcDynmap;
import org.dynmap.markers.Marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A property a population region can have.
 */
public enum RegionProperty {
    /**
     * Property which states that the region was paid for
     * with actual money. Changes the region's icons to a
     * special icon.
     */
     PAID_REGION {
        @Override
        public void onAdd(PopulationRegion region) {
            if(region.hasName() && !region.hasProperty(FORBIDS_MARKER)) {
                Marker marker =  FtcDynmap.getMarker(region);
                marker.setMarkerIcon(FtcDynmap.getSpecialIcon());
            }
        }

        @Override
        public void onRemove(PopulationRegion region) {
            if(region.hasName() && !region.hasProperty(FORBIDS_MARKER)) {
                Marker marker =  FtcDynmap.getMarker(region);
                marker.setMarkerIcon(FtcDynmap.getNormalIcon());
            }
        }
    },

    /**
     * Property which states that the region should not have a
     * dynmap marker
     */
    FORBIDS_MARKER {
        @Override
        public void onAdd(PopulationRegion region) {
            if(!region.hasName()) return;
            FtcDynmap.getMarker(region).deleteMarker();
        }

        @Override
        public void onRemove(PopulationRegion region) {
            if(!region.hasName()) return;
            FtcDynmap.createMarker(region);
        }
    },

    /**
     * Property which states that the region should not show
     * a residents sign on its pole and not show residents in
     * the /regionresidents command
     */
    HIDE_RESIDENTS {
        @Override
        public void onAdd(PopulationRegion region) {
            regen(region);
        }

        @Override
        public void onRemove(PopulationRegion region) {
            regen(region);
        }

        void regen(PopulationRegion data) {
            Regions.placePole(data);
        }
    };

    public abstract void onAdd(PopulationRegion region);
    public abstract void onRemove(PopulationRegion region);

    /**
     * Packs all properties into a single integer
     * @param properties The properties to pack
     * @return The packed properties
     */
    public static int pack(Collection<RegionProperty> properties) {
        int result = 0;

        for (var v: properties) {
            result |= (1 << v.ordinal());
        }

        return result;
    }

    /**
     * Unpacks all region properties in the given packed integer
     * @param flags The properties to unpack
     * @return The unpacked properties
     */
    public static Collection<RegionProperty> unpack(int flags) {
        List<RegionProperty> properties = new ArrayList<>();

        for (var p: values()) {
            if (((1 << p.ordinal()) & flags) == 0) {
                continue;
            }

            properties.add(p);
        }

        return properties;
    }
}
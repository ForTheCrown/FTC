package net.forthecrown.regions;

import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcDynmap;
import org.dynmap.markers.Marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum RegionProperty {
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
            Crown.getRegionManager().getGenerator().generate(data);
        }
    };

    public abstract void onAdd(PopulationRegion region);
    public abstract void onRemove(PopulationRegion region);

    public static int pack(Collection<RegionProperty> properties) {
        int result = 0;

        for (var v: properties) {
            result |= (1 << v.ordinal());
        }

        return result;
    }

    public static Collection<RegionProperty> unpack(int flags) {
        List<RegionProperty> properties = new ArrayList<>();

        for (var p: values()) {
            if (((1 << p.ordinal()) & flags) == 0) continue;
            properties.add(p);
        }

        return properties;
    }
}
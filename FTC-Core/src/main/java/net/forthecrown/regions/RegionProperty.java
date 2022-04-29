package net.forthecrown.regions;

import lombok.Getter;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcDynmap;
import org.dynmap.markers.Marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum RegionProperty {
     PAID_REGION {
        @Override
        public void onAdd(RegionData region) {
            if(region.hasName() && !region.hasProperty(FORBIDS_MARKER)) {
                Marker marker =  FtcDynmap.getMarker(region);
                marker.setMarkerIcon(FtcDynmap.getSpecialIcon());
            }
        }

        @Override
        public void onRemove(RegionData region) {
            if(region.hasName() && !region.hasProperty(FORBIDS_MARKER)) {
                Marker marker =  FtcDynmap.getMarker(region);
                marker.setMarkerIcon(FtcDynmap.getNormalIcon());
            }
        }
    },

    FORBIDS_MARKER {
        @Override
        public void onAdd(RegionData region) {
            if(!region.hasName()) return;
            FtcDynmap.getMarker(region).deleteMarker();
        }

        @Override
        public void onRemove(RegionData region) {
            if(!region.hasName()) return;
            FtcDynmap.createMarker(region);
        }
    },

    HIDE_RESIDENTS {
        @Override
        public void onAdd(RegionData region) {
            regen(region);
        }

        @Override
        public void onRemove(RegionData region) {
            regen(region);
        }

        void regen(RegionData data) {
            if (data instanceof PopulationRegion r) {
                Crown.getRegionManager().getGenerator().generate(r);
            }
        }
    };

    public abstract void onAdd(RegionData region);
    public abstract void onRemove(RegionData region);

    @Getter
    private final int bitFlag;

    RegionProperty() {
        bitFlag = 1 << ordinal();
    }

    public static int pack(Collection<RegionProperty> properties) {
        int result = 0;

        for (var v: properties) {
            result = result | v.getBitFlag();
        }

        return result;
    }

    public static Collection<RegionProperty> unpack(int flags) {
        List<RegionProperty> properties = new ArrayList<>();

        for (var p: values()) {
            if ((p.getBitFlag() & flags) == 0) continue;
            properties.add(p);
        }

        return properties;
    }
}
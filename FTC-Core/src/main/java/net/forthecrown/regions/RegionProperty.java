package net.forthecrown.regions;

import net.forthecrown.core.FtcDynmap;
import org.dynmap.markers.Marker;

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
    };

    public abstract void onAdd(RegionData region);
    public abstract void onRemove(RegionData region);
}

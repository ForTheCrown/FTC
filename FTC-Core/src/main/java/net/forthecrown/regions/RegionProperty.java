package net.forthecrown.regions;

import net.forthecrown.core.FtcDynmap;

public enum RegionProperty {
    PAID_REGION {
        @Override
        public void onAdd(RegionData region) {
            if(region.getMarker() != null) region.getMarker().setMarkerIcon(FtcDynmap.getSpecialIcon());
        }

        @Override
        public void onRemove(RegionData region) {
            if(region.getMarker() != null) region.getMarker().setMarkerIcon(FtcDynmap.getNormalIcon());
        }
    },
    FORBIDS_MARKER {
        @Override
        public void onAdd(RegionData region) {
            if(region.getMarker() != null) {
                region.getMarker().deleteMarker();
                region.setMarker(null);
            }
        }

        @Override
        public void onRemove(RegionData region) {
            if(region.getMarker() != null && region.hasName()) {
                region.setMarker(FtcDynmap.findMarker(region));
            }
        }
    };

    public abstract void onAdd(RegionData region);
    public abstract void onRemove(RegionData region);
}

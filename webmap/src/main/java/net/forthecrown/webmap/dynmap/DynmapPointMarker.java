package net.forthecrown.webmap.dynmap;

import java.util.Objects;
import net.forthecrown.webmap.MapIcon;
import net.forthecrown.webmap.MapLayer;
import net.forthecrown.webmap.MapPointMarker;
import org.dynmap.markers.Marker;

public class DynmapPointMarker extends DynmapMarker implements MapPointMarker {

  private final Marker marker;

  public DynmapPointMarker(Marker marker, MapLayer layer) {
    super(marker, layer);
    this.marker = marker;
  }

  @Override
  public double x() {
    return marker.getX();
  }

  @Override
  public double y() {
    return marker.getY();
  }

  @Override
  public double z() {
    return marker.getZ();
  }

  @Override
  public void setLocation(double x, double y, double z) {
    marker.setLocation(marker.getWorld(), x, y, z);
  }

  @Override
  public MapIcon getIcon() {
    return new DynmapIcon(marker.getMarkerIcon());
  }

  @Override
  public void setIcon(MapIcon icon) {
    Objects.requireNonNull(icon, "Null icon");

    if (!(icon instanceof DynmapIcon ico)) {
      return;
    }

    marker.setMarkerIcon(ico.icon);
  }
}

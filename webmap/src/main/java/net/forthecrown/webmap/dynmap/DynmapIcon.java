package net.forthecrown.webmap.dynmap;

import java.util.Objects;
import net.forthecrown.webmap.MapIcon;
import org.dynmap.markers.MarkerIcon;

public class DynmapIcon implements MapIcon {

  final MarkerIcon icon;

  public DynmapIcon(MarkerIcon icon) {
    this.icon = icon;
  }

  @Override
  public String getId() {
    return icon.getMarkerIconID();
  }

  @Override
  public String getName() {
    return icon.getMarkerIconLabel();
  }

  @Override
  public void setName(String name) {
    Objects.requireNonNull(name, "Null name");
    icon.setMarkerIconLabel(name);
  }

  @Override
  public void delete() {
    icon.deleteIcon();
  }
}

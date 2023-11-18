package net.forthecrown.webmap.dynmap;

import com.google.common.base.Strings;
import com.mojang.datafixers.util.Unit;
import net.forthecrown.utils.Result;
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
  public Result<Unit> setName(String name) {
    if (Strings.isNullOrEmpty(name)) {
      return Result.error("Null/empty string");
    }

    icon.setMarkerIconLabel(name);
    return Result.unit();
  }

  @Override
  public void delete() {
    icon.deleteIcon();
  }
}

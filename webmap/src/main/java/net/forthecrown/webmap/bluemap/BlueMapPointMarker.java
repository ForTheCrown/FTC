package net.forthecrown.webmap.bluemap;

import com.mojang.datafixers.util.Unit;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.forthecrown.utils.Result;
import net.forthecrown.webmap.MapIcon;
import net.forthecrown.webmap.MapPointMarker;

public class BlueMapPointMarker extends BlueMapMarker implements MapPointMarker {

  private final POIMarker marker;
  private MapIcon icon;

  public BlueMapPointMarker(BlueMapLayer layer, String id, POIMarker marker) {
    super(layer, id, marker);
    this.marker = marker;
  }

  @Override
  public double x() {
    return marker.getPosition().getX();
  }

  @Override
  public double y() {
    return marker.getPosition().getY();
  }

  @Override
  public double z() {
    return marker.getPosition().getZ();
  }

  @Override
  public void setLocation(double x, double y, double z) {
    marker.setPosition(x, y, z);
  }

  @Override
  public MapIcon getIcon() {
    if (icon != null) {
      return icon;
    }

    var address = marker.getIconAddress();
    var storage = layer.map.getAssetStorage();

    return new BlueMapIcon(address, storage);
  }

  @Override
  public Result<Unit> setIcon(MapIcon icon) {
    if (icon == null) {
      return Result.error("Null icon");
    }
    if (!(icon instanceof BlueMapIcon blu)) {
      return Result.error("Icon from a different implementation (How???)");
    }

    this.icon = blu;
    marker.setIcon(blu.path, 0, 0);

    return Result.unit();
  }
}

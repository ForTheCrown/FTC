package net.forthecrown.webmap.bluemap;

import de.bluecolored.bluemap.api.markers.DetailMarker;
import de.bluecolored.bluemap.api.markers.Marker;
import java.util.Objects;
import net.forthecrown.webmap.MapLayer;
import net.forthecrown.webmap.MapMarker;
import org.bukkit.World;

public abstract class BlueMapMarker implements MapMarker {

  final String id;
  final Marker marker;
  BlueMapLayer layer;

  public BlueMapMarker(BlueMapLayer layer, String id, Marker marker) {
    this.layer = layer;
    this.id = id;
    this.marker = marker;
  }

  @Override
  public MapLayer getLayer() {
    return layer;
  }

  @Override
  public void setLayer(MapLayer layer) {
    if (!(layer instanceof BlueMapLayer blu)) {
      return;
    }

    this.layer.set.remove(id);
    blu.set.put(id, marker);

    this.layer = blu;
  }

  @Override
  public World getWorld() {
    return layer.getWorld();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getName() {
    return marker.getLabel();
  }

  @Override
  public void setTitle(String title) {
    Objects.requireNonNull(title, "Null title");
    marker.setLabel(title);
  }

  @Override
  public String getDescription() {
    if (marker instanceof DetailMarker detailMarker) {
      return detailMarker.getDetail();
    }
    return "";
  }

  @Override
  public void setDescription(String description) {
    Objects.requireNonNull(description, "Null description");

    if (marker instanceof DetailMarker detailMarker) {
      detailMarker.setDetail(description);
    }
  }

  @Override
  public boolean isHtmlSupported() {
    return true;
  }

  @Override
  public void delete() {
    layer.set.remove(id);
  }
}

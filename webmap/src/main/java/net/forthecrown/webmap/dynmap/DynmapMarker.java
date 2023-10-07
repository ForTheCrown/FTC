package net.forthecrown.webmap.dynmap;

import java.util.Objects;
import net.forthecrown.webmap.MapLayer;
import net.forthecrown.webmap.MapMarker;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.dynmap.markers.MarkerDescription;

public abstract class DynmapMarker implements MapMarker {

  private final MarkerDescription description;
  private MapLayer layer;

  public DynmapMarker(MarkerDescription description, MapLayer layer) {
    this.description = description;
    this.layer = layer;
  }

  @Override
  public MapLayer getLayer() {
    return layer;
  }

  @Override
  public void setLayer(MapLayer layer) {
    Objects.requireNonNull(layer, "Null layer");

    if (!(layer instanceof DynmapLayer la)) {
      return;
    }

    description.setMarkerSet(la.set);
    this.layer = layer;
  }

  @Override
  public World getWorld() {
    return Bukkit.getWorld(description.getWorld());
  }

  @Override
  public String getId() {
    return description.getMarkerID();
  }

  @Override
  public String getName() {
    return description.getLabel();
  }

  @Override
  public void setTitle(String title) {
    Objects.requireNonNull(title, "Null title");
    description.setLabel(title);
  }

  @Override
  public String getDescription() {
    return description.getDescription();
  }

  @Override
  public void setDescription(String description) {
    this.description.setDescription(description);
  }

  @Override
  public boolean isHtmlSupported() {
    return description.isLabelMarkup();
  }

  @Override
  public void delete() {
    description.deleteMarker();
  }
}

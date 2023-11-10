package net.forthecrown.webmap.dynmap;

import com.google.common.base.Strings;
import com.mojang.datafixers.util.Unit;
import java.util.Objects;
import net.forthecrown.utils.Result;
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
  public Result<Unit> setLayer(MapLayer layer) {
    if (layer == null) {
      return Result.error("Null layer");
    }
    if (!(layer instanceof DynmapLayer la)) {
      return Result.error("Layer from a different implementation (How did this happen???)");
    }

    description.setMarkerSet(la.set);
    this.layer = layer;

    return Result.unit();
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
  public Result<Unit> setTitle(String title) {
    if (Strings.isNullOrEmpty(title)) {
      return Result.error("Null/empty title");
    }

    description.setLabel(title);
    return Result.unit();
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

package net.forthecrown.webmap.dynmap;

import com.google.common.base.Strings;
import com.mojang.datafixers.util.Unit;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import net.forthecrown.utils.Result;
import net.forthecrown.webmap.MapAreaMarker;
import net.forthecrown.webmap.MapIcon;
import net.forthecrown.webmap.MapLayer;
import net.forthecrown.webmap.MapPointMarker;
import net.forthecrown.webmap.WebMapUtils;
import org.bukkit.World;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerSet;

public class DynmapLayer implements MapLayer {

  final MarkerSet set;
  final World world;

  public DynmapLayer(MarkerSet set, World world) {
    this.set = set;
    this.world = world;
  }

  @Override
  public World getWorld() {
    return world;
  }

  @Override
  public String getName() {
    return set.getMarkerSetLabel();
  }

  @Override
  public String getId() {
    return set.getMarkerSetID();
  }

  @Override
  public Result<Unit> setName(String name) {
    if (Strings.isNullOrEmpty(name)) {
      return Result.error("Null/empty name");
    }

    set.setMarkerSetLabel(name);
    return Result.unit();
  }

  @Override
  public Collection<MapPointMarker> getPointMarkers() {
    return set.getMarkers().stream()
        .map(marker -> new DynmapPointMarker(marker, this))
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public Collection<MapAreaMarker> getAreaMarkers() {
    return set.getAreaMarkers().stream()
        .map(marker -> new DynmapAreaMarker(marker, this))
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public Optional<MapPointMarker> findPointMarker(String id) {
    if (Strings.isNullOrEmpty(id)) {
      return Optional.empty();
    }

    return Optional.ofNullable(set.findMarker(id))
        .map(marker -> new DynmapPointMarker(marker, this));
  }

  @Override
  public Result<MapPointMarker> createPointMarker(
      String id,
      String name,
      double x,
      double y,
      double z,
      MapIcon icon
  ) {
    if (Strings.isNullOrEmpty(id)) {
      return Result.error("Null/empty ID");
    }
    if (Strings.isNullOrEmpty(name)) {
      return Result.error("Null/empty marker name");
    }
    if (icon == null) {
      return Result.error("Null icon");
    }

    if (findPointMarker(id).isPresent()) {
      return Result.error("Point marker with ID '%s' already exists", id);
    }

    Marker marker = set.createMarker(
        id,
        name,
        DynmapWebmap.getId(world),
        x, y, z,
        DynmapWebmap.getIcon(icon),
        true
    );

    return Result.success(new DynmapPointMarker(marker, this));
  }

  @Override
  public Optional<MapAreaMarker> findAreaMarker(String id) {
    if (Strings.isNullOrEmpty(id)) {
      return Optional.empty();
    }

    return Optional.ofNullable(set.findAreaMarker(id))
        .map(m -> new DynmapAreaMarker(m, this));
  }

  @Override
  public Result<MapAreaMarker> createAreaMarker(
      String id,
      String name,
      double[] xCorners,
      double[] zCorners
  ) {
    if (Strings.isNullOrEmpty(id)) {
      return Result.error("Null/empty ID");
    }
    if (Strings.isNullOrEmpty(name)) {
      return Result.error("Null/empty marker name");
    }

    var cornersResult = WebMapUtils.validateAreaCoordinates(xCorners, zCorners);
    if (cornersResult.isError()) {
      return cornersResult.cast();
    }

    if (findAreaMarker(id).isPresent()) {
      return Result.error("Area marker with ID '%s' already exists", id);
    }

    AreaMarker marker = set.createAreaMarker(
        id, name,
        true,
        DynmapWebmap.getId(world),
        xCorners,
        zCorners,
        true
    );

    return Result.success(new DynmapAreaMarker(marker, this));
  }

  @Override
  public void delete() {
    set.deleteMarkerSet();
  }
}

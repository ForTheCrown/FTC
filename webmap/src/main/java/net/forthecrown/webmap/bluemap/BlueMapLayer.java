package net.forthecrown.webmap.bluemap;

import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.Result;
import net.forthecrown.webmap.MapAreaMarker;
import net.forthecrown.webmap.MapIcon;
import net.forthecrown.webmap.MapLayer;
import net.forthecrown.webmap.MapPointMarker;
import org.bukkit.World;

@RequiredArgsConstructor
public class BlueMapLayer implements MapLayer {

  final String id;
  final MarkerSet set;
  final World world;
  final BlueMapMap map;
  final BlueWebmap webmap;

  @Override
  public World getWorld() {
    return world;
  }

  @Override
  public String getName() {
    return set.getLabel();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setName(String name) {
    set.setLabel(name);
  }

  @Override
  public Collection<MapPointMarker> getPointMarkers() {
    return set.getMarkers().entrySet().stream()
        .filter(marker -> marker.getValue() instanceof POIMarker)
        .map(e -> new BlueMapPointMarker(this, e.getKey(), (POIMarker) e.getValue()))
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public Collection<MapAreaMarker> getAreaMarkers() {
    return set.getMarkers().entrySet().stream()
        .filter(marker -> marker.getValue() instanceof ExtrudeMarker)
        .map(e -> new BlueMapAreaMarker(this, e.getKey(), (ExtrudeMarker) e.getValue()))
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public Optional<MapPointMarker> findPointMarker(String id) {
    return Optional.ofNullable(set.get(id))
        .filter(marker -> marker instanceof POIMarker)
        .map(marker -> new BlueMapPointMarker(this, id, (POIMarker) marker));
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
    if (findPointMarker(id).isPresent()) {
      return Result.error("Marker with ID '%s' is already defined", id);
    }

    var builder = POIMarker.builder()
        .label(name)
        .position(x, y, z);

    if (icon instanceof BlueMapIcon blu) {
      builder.icon(blu.path, 0, 0);
    }

    var marker = builder.build();
    set.put(id, marker);

    return Result.success(new BlueMapPointMarker(this, id, marker));
  }

  @Override
  public Optional<MapAreaMarker> findAreaMarker(String id) {
    return Optional.ofNullable(set.get(id))
        .filter(marker -> marker instanceof ExtrudeMarker)
        .map(marker -> new BlueMapAreaMarker(this, id, (ExtrudeMarker) marker));
  }

  @Override
  public Result<MapAreaMarker> createAreaMarker(
      String id,
      String name,
      double[] xCorners,
      double[] zCorners
  ) {
    if (findAreaMarker(id).isPresent()) {
      return Result.error("Marker with ID '%s' is already defined", id);
    }

    var builder = ExtrudeMarker.builder();
    builder.label(name);
    builder.shape(
        BlueMapAreaMarker.shapeFromPoints(xCorners, zCorners),
        world.getMinHeight(),
        world.getMaxHeight()
    );

    var marker = builder.build();
    set.put(id, marker);

    return Result.success(new BlueMapAreaMarker(this, id, marker));
  }

  @Override
  public void delete() {
    map.getMarkerSets().remove(id);
  }
}

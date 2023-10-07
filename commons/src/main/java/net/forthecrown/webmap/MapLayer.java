package net.forthecrown.webmap;

import java.util.Collection;
import java.util.Optional;
import net.forthecrown.utils.Result;
import org.bukkit.World;

public interface MapLayer {

  World getWorld();

  String getName();

  String getId();

  void setName(String name);

  Collection<MapPointMarker> getPointMarkers();

  Collection<MapAreaMarker> getAreaMarkers();

  Optional<MapPointMarker> findPointMarker(String id);

  Result<MapPointMarker> createPointMarker(
      String id,
      String name,
      double x,
      double y,
      double z,
      MapIcon icon
  );

  Optional<MapAreaMarker> findAreaMarker(String id);

  Result<MapAreaMarker> createAreaMarker(
      String id,
      String name,
      double[] xCorners,
      double[] zCorners
  );

  void delete();
}

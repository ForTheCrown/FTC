package net.forthecrown.webmap;

import org.bukkit.World;

public interface MapMarker {

  MapLayer getLayer();

  void setLayer(MapLayer layer);

  World getWorld();

  String getId();

  String getName();

  void setTitle(String title);

  String getDescription();

  void setDescription(String description);

  boolean isHtmlSupported();

  void delete();
}

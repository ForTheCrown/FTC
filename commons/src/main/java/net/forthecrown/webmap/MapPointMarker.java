package net.forthecrown.webmap;

public interface MapPointMarker extends MapMarker {

  double x();

  double y();

  double z();

  void setLocation(double x, double y, double z);

  MapIcon getIcon();

  void setIcon(MapIcon icon);
}
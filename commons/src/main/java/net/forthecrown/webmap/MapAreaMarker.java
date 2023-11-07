package net.forthecrown.webmap;

import javax.annotation.Nullable;
import org.bukkit.Color;

public interface MapAreaMarker extends MapMarker {

  double[] getXCorners();

  double[] getZCorners();

  void setCorners(double[] x, double[] z);

  double getMinY();

  double getMaxY();

  void setYBounds(double a, double b);

  Color getFillColor();

  void setFillColor(@Nullable Color color);

  Color getLineColor();

  void setLineColor(@Nullable Color color);

  int getLineSize();

  void setLineSize(int size);
}

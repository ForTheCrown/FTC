package net.forthecrown.webmap.dynmap;

import com.mojang.datafixers.util.Unit;
import net.forthecrown.utils.Result;
import net.forthecrown.webmap.MapAreaMarker;
import net.forthecrown.webmap.MapLayer;
import net.forthecrown.webmap.WebMapUtils;
import org.bukkit.Color;
import org.dynmap.markers.AreaMarker;
import org.jetbrains.annotations.Nullable;

public class DynmapAreaMarker extends DynmapMarker implements MapAreaMarker {

  private final AreaMarker marker;

  public DynmapAreaMarker(AreaMarker areaMarker, MapLayer layer) {
    super(areaMarker, layer);
    this.marker = areaMarker;
  }

  @Override
  public double[] getXCorners() {
    double[] corners = new double[marker.getCornerCount()];
    for (int i = 0; i < corners.length; i++) {
      corners[i] = marker.getCornerX(i);
    }
    return corners;
  }

  @Override
  public double[] getZCorners() {
    double[] corners = new double[marker.getCornerCount()];
    for (int i = 0; i < corners.length; i++) {
      corners[i] = marker.getCornerZ(i);
    }
    return corners;
  }

  @Override
  public Result<Unit> setCorners(double[] x, double[] z) {
    return WebMapUtils.validateAreaCoordinates(x, z).map(unit -> {
      marker.setCornerLocations(x, z);
      return unit;
    });
  }

  @Override
  public double getMinY() {
    return marker.getBottomY();
  }

  @Override
  public double getMaxY() {
    return marker.getTopY();
  }

  @Override
  public void setYBounds(double a, double b) {
    marker.setRangeY(Math.min(a, b), Math.max(a, b));
  }

  static Color fromDynmapValues(double opacity, int rgb) {
    int a = (int) (255 * opacity);
    int r = (rgb >> 4) & 0xFF;
    int g = (rgb >> 2) & 0xFF;
    int b = (rgb >> 0) & 0xFF;
    return Color.fromARGB(a, r, g, b);
  }

  static double opacity(Color color) {
    return color != null
        ? ((double) color.getAlpha()) / 255.0
        : 0;
  }

  static int rgb(Color color) {
    return color != null
        ? color.asRGB()
        : 0;
  }

  @Override
  public Color getFillColor() {
    return fromDynmapValues(marker.getFillOpacity(), marker.getFillColor());
  }

  @Override
  public void setFillColor(@Nullable Color color) {
    marker.setFillStyle(opacity(color), rgb(color));
  }

  @Override
  public Color getLineColor() {
    return fromDynmapValues(marker.getFillOpacity(), marker.getFillColor());
  }

  @Override
  public void setLineColor(@Nullable Color color) {
    marker.setLineStyle(marker.getLineWeight(), opacity(color), rgb(color));
  }

  @Override
  public int getLineSize() {
    return marker.getLineWeight();
  }

  @Override
  public void setLineSize(int size) {
    marker.setLineStyle(size, marker.getLineOpacity(), marker.getLineColor());
  }

  @Override
  public boolean holesSupported() {
    return false;
  }

  @Override
  public void clearHoles() {
    // No op, dynmap doesn't support holes
  }

  @Override
  public Result<Unit> addHole(double[] xCorners, double[] zCorners) {
    // No op, dynmap doesn't support holes
    return Result.unit();
  }

  @Override
  public void removeHole(int index) {
    // No op, dynmap doesn't support holes
  }

  @Override
  public int getHolesSize() {
    return 0;
  }

  @Override
  public double[][] getHoles() {
    return WebMapUtils.EMPTY_CORNERS;
  }
}

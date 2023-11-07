package net.forthecrown.webmap.bluemap;

import com.flowpowered.math.vector.Vector2d;
import com.google.common.base.Preconditions;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.math.Shape;
import java.util.Objects;
import net.forthecrown.webmap.MapAreaMarker;
import org.bukkit.Color;
import org.jetbrains.annotations.Nullable;

public class BlueMapAreaMarker extends BlueMapMarker implements MapAreaMarker {

  private final ExtrudeMarker marker;

  public BlueMapAreaMarker(BlueMapLayer layer, String id, ExtrudeMarker marker) {
    super(layer, id, marker);
    this.marker = marker;
  }

  @Override
  public double[] getXCorners() {
    var shape = marker.getShape();
    double[] points = new double[shape.getPointCount()];

    for (int i = 0; i < points.length; i++) {
      var point = shape.getPoint(i);
      points[i] = point.getX();
    }

    return points;
  }

  @Override
  public double[] getZCorners() {
    var shape = marker.getShape();
    double[] points = new double[shape.getPointCount()];

    for (int i = 0; i < points.length; i++) {
      var point = shape.getPoint(i);
      points[i] = point.getY();
    }

    return points;
  }

  @Override
  public void setCorners(double[] x, double[] z) {
    Shape shape = shapeFromPoints(x, z);
    marker.setShape(shape, marker.getShapeMinY(), marker.getShapeMaxY());
  }

  static Shape shapeFromPoints(double[] x, double[] z) {
    Objects.requireNonNull(x, "Null x points");
    Objects.requireNonNull(z, "Null z points");
    Preconditions.checkArgument(x.length == z.length, "points arrays size mismatch");

    // Compatibility with Dynmap, this would throw an exception if we used
    // the normal shape constructor
    if (x.length == 2) {
      return Shape.createRect(x[0], z[0], x[1], z[1]);
    }

    Vector2d[] points = new Vector2d[x.length];

    for (int i = 0; i < points.length; i++) {
      points[i] = new Vector2d(x[i], z[i]);
    }

    return new Shape(points);
  }

  @Override
  public double getMinY() {
    return marker.getShapeMinY();
  }

  @Override
  public double getMaxY() {
    return marker.getShapeMaxY();
  }

  @Override
  public void setYBounds(double a, double b) {
    marker.setShape(
        marker.getShape(),
        (float) Math.min(a, b),
        (float) Math.max(a, b)
    );
  }

  private de.bluecolored.bluemap.api.math.Color toApiColor(Color color) {
    if (color == null) {
      return null;
    }
    return new de.bluecolored.bluemap.api.math.Color(color.asARGB());
  }

  private Color fromApiColor(de.bluecolored.bluemap.api.math.Color color) {
    if (color == null) {
      return null;
    }
    int a = (int) (color.getAlpha() * 255.0d);
    return Color.fromARGB(a, color.getRed(), color.getGreen(), color.getBlue());
  }

  @Override
  public Color getFillColor() {
    return fromApiColor(marker.getFillColor());
  }

  @Override
  public void setFillColor(@Nullable Color color) {
    marker.setFillColor(toApiColor(color));
  }

  @Override
  public Color getLineColor() {
    return fromApiColor(marker.getLineColor());
  }

  @Override
  public void setLineColor(@Nullable Color color) {
    marker.setLineColor(toApiColor(color));
  }

  @Override
  public int getLineSize() {
    return marker.getLineWidth();
  }

  @Override
  public void setLineSize(int size) {
    marker.setLineWidth(size);
  }
}

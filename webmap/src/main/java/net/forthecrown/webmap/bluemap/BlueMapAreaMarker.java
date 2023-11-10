package net.forthecrown.webmap.bluemap;

import com.flowpowered.math.vector.Vector2d;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Unit;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.math.Shape;
import java.util.List;
import java.util.Objects;
import net.forthecrown.utils.Result;
import net.forthecrown.webmap.MapAreaMarker;
import net.forthecrown.webmap.WebMapUtils;
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
  public Result<Unit> setCorners(double[] x, double[] z) {
    return shapeFromPoints(x, z).map(shape -> {
      marker.setShape(shape, marker.getShapeMinY(), marker.getShapeMaxY());
      return Unit.INSTANCE;
    });
  }

  static Result<Shape> shapeFromPoints(double[] x, double[] z) {
    var validation = WebMapUtils.validateAreaCoordinates(x, z);
    if (validation.isError()) {
      return validation.cast();
    }

    // Compatibility with Dynmap, this would throw an exception if we used
    // the normal shape constructor
    if (x.length == 2) {
      return Result.success(Shape.createRect(x[0], z[0], x[1], z[1]));
    }

    Vector2d[] points = new Vector2d[x.length];

    for (int i = 0; i < points.length; i++) {
      points[i] = new Vector2d(x[i], z[i]);
    }

    return Result.success(new Shape(points));
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
      return new de.bluecolored.bluemap.api.math.Color(0, 0, 0, 0f);
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

  @Override
  public boolean holesSupported() {
    return true;
  }

  @Override
  public void clearHoles() {
    marker.getHoles().clear();
  }

  @Override
  public Result<Unit> addHole(double[] xCorners, double[] zCorners) {
    return shapeFromPoints(xCorners, zCorners).map(shape -> {
      marker.getHoles().add(shape);
      return Unit.INSTANCE;
    });
  }

  @Override
  public void removeHole(int index) {
    Objects.checkIndex(index, getHolesSize());
    var holes = marker.getHoles();

    if (holes instanceof List<Shape> list) {
      list.remove(index);
      return;
    }

    var it = holes.iterator();
    int i = 0;

    while (it.hasNext()) {
      var n = it.next();

      if (i == index) {
        it.remove();
        break;
      }

      i++;
    }
  }

  @Override
  public int getHolesSize() {
    return marker.getHoles().size();
  }

  @Override
  public double[][] getHoles() {
    return new double[0][];
  }
}

/*

        "${prefix} &6Server Rules:"
        "\n&c1. &rNo hacking or using xray!",
        "\n&c2. &rBe respectful to other players!",
        "\n&c3. &rNo spamming or advertising!",
        "\n&c4. &rNo unwanted PvP!",
        "\n&c5. &rNo impersonating other players!",
        "\n&c6. &rOnly play on 1 account at a time!",
        "\n&c7. &rNo lag machines!",
        "\n&c8. &rDo not use exploits, report them!",
        "\n&c9. &rDo not use mods that change gameplay!",
 */
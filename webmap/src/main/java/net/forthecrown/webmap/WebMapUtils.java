package net.forthecrown.webmap;

import com.mojang.datafixers.util.Unit;
import net.forthecrown.utils.Result;

public final class WebMapUtils {
  private WebMapUtils() {}

  public static final double[][] EMPTY_CORNERS = new double[0][];

  public static Result<Unit> validateAreaCoordinates(double[] x, double[] z) {
    if (x == null || x.length < 1) {
      return Result.error("Null/empty x coordinates");
    }
    if (z == null || z.length < 1) {
      return Result.error("Null/empty z coordinates");
    }
    if (z.length != x.length) {
      return Result.error(
          "Points array size mismatch (x and z coordinates must have equal amounts of points)"
      );
    }

    return Result.unit();
  }
}

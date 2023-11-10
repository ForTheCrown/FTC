package net.forthecrown.webmap;

import com.mojang.datafixers.util.Unit;
import javax.annotation.Nullable;
import net.forthecrown.utils.Result;
import org.bukkit.Color;
import org.spongepowered.math.vector.Vector2d;

/**
 * Marker that encompasses a polygonal area with a height ranging between 2 user-defined Y levels
 */
public interface MapAreaMarker extends MapMarker {

  /**
   * Gets the shape's X coordinate corners
   * @return X coordinate corners
   */
  double[] getXCorners();

  /**
   * Gets the shape's Z coordinate corners
   * @return Z coordinate corners
   */
  double[] getZCorners();

  /**
   * Sets the marker's corners.
   * <p>
   * This method may return an erroneous reesult in any of the following cases:
   * <ol>
   *   <li>The specified {@code x} array is null/empty</li>
   *   <li>The specified {@code z} array is null/empty</li>
   *   <li>The specified {@code x} and {@code z} arrays don't have matching sizes</li>
   * </ol>
   *
   * @param x X coordinate corners
   * @param z Z coordinate corners
   *
   * @return Edit result
   */
  Result<Unit> setCorners(double[] x, double[] z);

  /**
   * Gets the marker's Minimum Y point
   * @return Minimum Y point
   */
  double getMinY();

  /**
   * Gets the marker's Maximum Y point
   * @return Maximum Y point
   */
  double getMaxY();

  /**
   * Sets the y bounds of a marker. Parameter order doesn't matter as both {@code a} and {@code b}
   * are {@link Math#min(int, int)}-ed and {@link Math#max(int, int)}-ed
   *
   * @param a First Y level
   * @param b Second Y level
   */
  void setYBounds(double a, double b);

  /**
   * Gets the marker area's fill color (Opacity included in the color's alpha)
   * @return Area fill color
   */
  Color getFillColor();

  /**
   * Sets the marker's fill color. (Area opacity set by the color's alpha component)
   * @param color New fill color
   * @implNote If the underlying web map plugin doesn't allow 'null' colors, set it to a
   *           transparent black (AARRGGBB 0x00000000)
   */
  void setFillColor(@Nullable Color color);

  /**
   * Gets the marker's line color (Opacity included in the color's alpha)
   * @return Line color
   */
  Color getLineColor();

  /**
   * Sets the marker's line color (Opacit set by the color's alpha component)
   * @param color New line color, or {@code null}, to remove it.
   * @implNote If the underlying web map plugin doesn't allow 'null' colors, set it to a
   *           transparent black (AARRGGBB 0x00000000)
   */
  void setLineColor(@Nullable Color color);

  /**
   * Gets the marker's line size
   * @return Line size
   */
  int getLineSize();

  /**
   * Sets the marker's line size
   * @param size New line size
   */
  void setLineSize(int size);

  /**
   * Tests whether holes in the marker's shape are supported
   * @return {@code true} if holes are supported, {@code false} otherwise
   */
  boolean holesSupported();

  /**
   * Clears all shape holes
   * @implNote If implementation doesn't support holes: No-op
   */
  void clearHoles();

  /**
   * Adds a hole into the marker's shape. Works similarly to {@link #setCorners(double[], double[])}
   * except the corners subtract from the marker shape's area.
   *
   * <p>
   * This method may return an erroneous reesult in any of the following cases:
   * <ol>
   *   <li>The specified {@code x} array is null/empty</li>
   *   <li>The specified {@code z} array is null/empty</li>
   *   <li>The specified {@code x} and {@code z} arrays don't have matching sizes</li>
   * </ol>
   *
   * @param xCorners X coordinate corners
   * @param zCorners Z coordinate corners
   *
   * @implNote If implementation doesn't support holes: No-op
   */
  Result<Unit> addHole(double[] xCorners, double[] zCorners);

  /**
   * Removes a hole
   *
   * @param index Index of the hole to remove
   *
   * @throws IndexOutOfBoundsException If the specified {@code index} is out of bounds
   * @implNote If implementation doesn't support holes: No-op
   */
  void removeHole(int index);

  /**
   * Gets the amount of holes the marker's shape currently has
   * @return Hole count
   * @implNote If implementation doesn't support holes: return {@code 0}
   */
  int getHolesSize();

  /**
   * Gets a cloned array of shape holes a marker has
   * @return Holes
   * @implNote If implementation doesn't support holes: return an empty array
   */
  double[][] getHoles();
}

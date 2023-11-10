package net.forthecrown.webmap;

import com.mojang.datafixers.util.Unit;
import net.forthecrown.utils.Result;

/**
 * Marker located at a single point in a world with an icon to represent it
 */
public interface MapPointMarker extends MapMarker {

  /**
   * Gets a marker's X position
   * @return X position
   */
  double x();

  /**
   * Gets a marker's Y position
   * @return Y position
   */
  double y();

  /**
   * Gets a marker's Z position
   * @return Z position
   */
  double z();

  /**
   * Sets a marker's location
   * @param x X coordinate
   * @param y Y coordinate
   * @param z Z coordinate
   */
  void setLocation(double x, double y, double z);

  /**
   * Gets a marker's icon
   * @return Marker Icon
   */
  MapIcon getIcon();

  /**
   * Sets a marker's icon
   * <p>
   * This method may return an erroneous reesult in any of the following cases:
   * <ol>
   *   <li>The specified {@code icon} is null/empty</li>
   *   <li>The specified {@code icon} is from a different web map implementation</li>
   * </ol>
   *
   * @param icon new icon
   * @return Edit result
   */
  Result<Unit> setIcon(MapIcon icon);
}
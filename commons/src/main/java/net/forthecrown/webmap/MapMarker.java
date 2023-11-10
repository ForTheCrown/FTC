package net.forthecrown.webmap;

import com.mojang.datafixers.util.Unit;
import net.forthecrown.utils.Result;
import org.bukkit.World;

/**
 * Common interface for point markers and area markers
 */
public interface MapMarker {

  /**
   * Gets the map layer
   * @return Map layer
   */
  MapLayer getLayer();

  /**
   * Sets the marker's layer
   * <p>
   * This method may return an erroneous reesult in any of the following cases:
   * <ol>
   *   <li>The specified {@code layer} is null/empty</li>
   *   <li>The specified {@code layer} is from a different implementation of the webmap</li>
   * </ol>
   *
   * @param layer New layer
   * @return Edit result
   */
  Result<Unit> setLayer(MapLayer layer);

  /**
   * Gets the marker's world
   * @return Marker's world
   */
  World getWorld();

  /**
   * Gets the marker's ID
   * @return Marker ID
   */
  String getId();

  /**
   * Gets the marker's display name
   * @return display name
   */
  String getName();

  /**
   * Sets the marker's title.
   * <p>
   * This method may return an erroneous reesult in any of the following cases:
   * <ol>
   *   <li>The specified {@code title} is null/empty</li>
   * </ol>
   *
   * @param title New title
   * @return Edit result
   */
  Result<Unit> setTitle(String title);

  /**
   * Gets the marker's description
   * @return Marker description
   */
  String getDescription();

  /**
   * Sets a marker's description
   * @param description New description
   */
  void setDescription(String description);

  /**
   * Tests whether the description supports HTML
   * @return {@code true}, if HTML is supported, {@code false} otherwise
   */
  boolean isHtmlSupported();

  /**
   * Deletes the marker
   */
  void delete();
}

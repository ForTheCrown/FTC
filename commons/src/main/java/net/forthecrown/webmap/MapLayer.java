package net.forthecrown.webmap;

import com.mojang.datafixers.util.Unit;
import java.util.Collection;
import java.util.Optional;
import net.forthecrown.utils.Result;
import org.bukkit.World;

/**
 * A map layer (also known as a 'marker set' in both BlueMap and Dynmap) is a set of markers. All
 * markers on a layer can be disabled/enabled by a player when viewing the web map.
 *
 */
public interface MapLayer {

  /**
   * The world the layer belongs to
   * @return Layer's world
   */
  World getWorld();

  /**
   * Gets the layer's display name.
   * @return Layer's display name
   */
  String getName();

  /**
   * Gets the layer's ID
   * @return Layer's ID
   */
  String getId();

  /**
   * Sets the layer's name.
   *
   * @param name The layer's new display name
   *
   * @return An erroneous result if the name is null or empty, otherwise a successful result is
   *         returned
   */
  Result<Unit> setName(String name);

  /**
   * Gets a collection of all point markers in this layer
   * @return Point markers
   */
  Collection<MapPointMarker> getPointMarkers();

  /**
   * Gets a list of all area markers in this layer
   * @return Layer's area markers
   */
  Collection<MapAreaMarker> getAreaMarkers();

  /**
   * Finds a point marker.
   *
   * @param id Marker ID
   *
   * @return An empty result if the ID is null/empty or if the marker couldn't be found or if the
   *        marker wasn't a point marker. Otherwise, returns a result containing the marker
   *
   * @see MapPointMarker
   * @see #createPointMarker(String, String, double, double, double, MapIcon)
   */
  Optional<MapPointMarker> findPointMarker(String id);

  /**
   * Creates a point marker.
   * <p>
   * This method may return an erroneous reesult in any of the following cases:
   * <ol>
   *   <li>The specified {@code id} is null/empty</li>
   *   <li>The specified {@code name} is null/empty</li>
   *   <li>The specified {@code icon} is null</li>
   *   <li>A marker with the specified {@code id} already exists</li>
   * </ol>
   *
   * @param id   ID
   * @param name display name
   * @param x    X position
   * @param y    Y position
   * @param z    Z position
   * @param icon Marker's icon
   *
   * @return An erroneous result if either id or name is null/empty, if the icon is null or if a
   *         marker with the specified ID already exists. Otherwise, a successful result is returned
   */
  Result<MapPointMarker> createPointMarker(
      String id,
      String name,
      double x,
      double y,
      double z,
      MapIcon icon
  );

  /**
   * Finds an area marker
   *
   * @param id Marker ID
   *
   * @return An empty result if the id was null, or if the marker wasn't found, or if it wasn't an
   *         area marker. Otherwise, the found marker is returned
   *
   * @see MapAreaMarker
   * @see #createAreaMarker(String, String, double[], double[])
   */
  Optional<MapAreaMarker> findAreaMarker(String id);

  /**
   * Creates an area marker
   * <p>
   * This method may return an erroneous reesult in any of the following cases:
   * <ol>
   *   <li>The specified {@code id} is null/empty</li>
   *   <li>The specified {@code name} is null/empty</li>
   *   <li>The specified {@code xCorners} is null/empty</li>
   *   <li>The specified {@code zCorners} is null/empty</li>
   *   <li>A marker with the specified {@code id} already exists</li>
   *   <li>The size of xCorners and zCorners isn't equal</li>
   * </ol>
   *
   * @param id       ID
   * @param name     display name
   * @param xCorners X corners
   * @param zCorners Z corners
   *
   * @return An erroneous result if any of the above conditions were met. Otherwise, a successful
   *         result containing the marker is returned
   */
  Result<MapAreaMarker> createAreaMarker(
      String id,
      String name,
      double[] xCorners,
      double[] zCorners
  );

  /**
   * Deletes this layer
   */
  void delete();
}

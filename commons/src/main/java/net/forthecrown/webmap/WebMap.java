package net.forthecrown.webmap;

import java.io.InputStream;
import java.util.Optional;
import net.forthecrown.BukkitServices;
import net.forthecrown.utils.Result;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract interface for representing an API that interacts with a web-based map like BlueMap or
 * Dynmap
 */
public interface WebMap {

  /**
   * Gets the current WebMap implementation, (Implemented by the 'FTC-Webmap' plugin), or
   * a no-op implementation in case the map plugin was disabled or failed to initalize
   *
   * @return WebMap implementation
   */
  static WebMap map() {
    return BukkitServices.load(WebMap.class).orElse(NopMap.NOP);
  }

  /**
   * Gets a map layer
   * <p>
   * A map layer (also called a 'marker set' in both BlueMap and Dynmap) is just a set of markers or
   * other objects.
   *
   * @param world World the layer is in
   * @param id string ID of the layer
   *
   * @return Empty optional if the layer wasn't found, or any of the parameters were null.
   *         Otherwise, an optional containing the wrapped layer is returned
   */
  Optional<MapLayer> getLayer(@NotNull World world, String id);

  /**
   * Creates a layer
   * <p>
   * This method may return an erroneous reesult in any of the following cases:
   * <ol>
   *   <li>The specified {@code id} is null/empty</li>
   *   <li>The specified {@code name} is null/empty</li>
   *   <li>The specified {@code world} is null</li>
   *   <li>A layer with the specified {@code id} already exists</li>
   *   <li>The hooked WebMap plugin doesn't support the specified world</li>
   *   <li>The hooked WebMap plugin was disabled</li>
   * </ol>
   *
   * @param world Layer's world
   * @param id    Layer ID
   * @param name  Layer label, will be shown to players on the website. Used when listing which
   *              layers exist for enabling/disabling.
   *
   * @return A result containing an error if the layer already exists or if world/id/name were
   *         {@code null}. Otherwise, a successful result containing the layer is returned.
   */
  Result<MapLayer> createLayer(@NotNull World world, String id, String name);

  /**
   * Gets a map icon by its ID
   * @param id icon ID
   * @return Empty result if the ID was {@code null}, or if the icon wasn't found
   */
  Optional<MapIcon> getIcon(String id);

  /**
   * Creates a map icon.
   * <p>
   * This method may return an erroneous reesult in any of the following cases:
   * <ol>
   *   <li>The specified {@code id} is null/empty</li>
   *   <li>The specified {@code name} is null/empty</li>
   *   <li>The specified {@code iconData} is null</li>
   *   <li>An icon with the specified {@code id} already exists</li>
   *   <li>The hooked WebMap plugin was disabled</li>
   * </ol>
   *
   * @param id Icon ID
   * @param name Icon name
   * @param iconData Icon data, in PNG format
   *
   *  @return A result containing an error if the icon already exists or if iconData/id/name were
   *          {@code null}. Otherwise, a successful result containing the icon is returned.
   */
  Result<MapIcon> createIcon(String id, String name, InputStream iconData);

  /**
   * Tests if a player is visible on the WebMap
   *
   * @param player Player to test
   *
   * @return {@code true}, if the player is marked as being visible on the web map, {@code false}
   *         otherwise
   *
   * @throws NullPointerException If the specified {@code player} is {@code null}
   */
  boolean isPlayerVisible(OfflinePlayer player);

  /**
   * Sets if a player is displayed on the web map or not
   * @param player Player
   * @param visible Display state
   * @throws NullPointerException If the specified {@code player} is {@code null}
   */
  void setPlayerVisible(OfflinePlayer player, boolean visible);
}

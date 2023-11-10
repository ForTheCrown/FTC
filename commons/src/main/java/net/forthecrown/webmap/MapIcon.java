package net.forthecrown.webmap;

import com.mojang.datafixers.util.Unit;
import net.forthecrown.utils.Result;

/**
 * Map icon used for point markers
 */
public interface MapIcon {

  /**
   * Gets the icon's ID
   * @return Icon ID
   */
  String getId();

  /**
   * Gets the icon's display name
   * @return Display name
   */
  String getName();

  /**
   * Sets the icon's display name
   * @param name New display name
   * @return Edit result
   */
  Result<Unit> setName(String name);

  /**
   * Deletes the icon
   */
  void delete();
}

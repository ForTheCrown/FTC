package net.forthecrown.utils;

import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public final class Locations {
  private Locations() {}

  /**
   * Clones a location or returns a null value if the input is null
   * @param location Location to clone
   * @return Cloned location, or {@code null}, if the specified {@code location} was null
   */
  @Contract("null -> null; !null -> !null")
  public static @Nullable Location clone(@Nullable Location location) {
    return location == null ? null : location.clone();
  }
}
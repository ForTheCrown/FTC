package net.forthecrown;

import static org.bukkit.NamespacedKey.minecraft;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

public final class Worlds {
  private Worlds() {}

  // World names
  public static final String VOID_NAME = "void";
  public static final String RESOURCE_NAME = "world_resource";

  // World keys
  public static final NamespacedKey OVERWORLD_KEY = minecraft("overworld");
  public static final NamespacedKey END_KEY       = minecraft("the_end");
  public static final NamespacedKey NETHER_KEY    = minecraft("the_nether");
  public static final NamespacedKey VOID_KEY      = minecraft(VOID_NAME);
  public static final NamespacedKey RW_KEY        = minecraft(RESOURCE_NAME);

  public static World nonNull(NamespacedKey key) {
    return Objects.requireNonNull(Bukkit.getWorld(key), "Unknown world: '" + key + "'");
  }

  public static World overworld() {
    return nonNull(OVERWORLD_KEY);
  }

  /**
   * Gets the void world
   *
   * @return The Void World
   */
  public static World voidWorld() {
    return nonNull(VOID_KEY);
  }

  /**
   * Gets the end
   *
   * @return The End World
   */
  public static World end() {
    return nonNull(END_KEY);
  }

  /**
   * Gets the resource world
   *
   * @return The Resource World
   */
  public static World resource() {
    return nonNull(RW_KEY);
  }

  /**
   * Gets the nether world
   *
   * @return The Nether world
   */
  public static World nether() {
    return nonNull(NETHER_KEY);
  }
}
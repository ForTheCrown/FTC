package net.forthecrown;

import static org.bukkit.NamespacedKey.minecraft;

import java.nio.file.Path;
import java.util.Objects;
import net.forthecrown.utils.io.PathUtil;
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

  /**
   * Destroys the world, unloading it and then deleting all of it's data
   * @param world World to delete
   * @return {@code false}, if {@link org.bukkit.Server#unloadWorld(String, boolean)} fails
   */
  public static boolean desroyWorld(World world) {
    Path worldDatafile = world.getWorldFolder().toPath();

    if (Bukkit.isTickingWorlds()) {
      Loggers.getLogger().warn("Unloading world {} mid-tick, this may be dangerous",
          world.getName()
      );
    }

    if (!Bukkit.unloadWorld(world, false)) {
      return false;
    }

    PathUtil.safeDelete(worldDatafile, true, true);
    return true;
  }
}
package net.forthecrown.worldloader;

import java.util.concurrent.CompletableFuture;
import net.forthecrown.BukkitServices;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service used to pre-generate worlds
 *
 * @apiNote unless implemented by a separate plugin will return a no-op instance of this class
 * that does nothing
 *
 * @see #loadWorld(World)
 * @see WorldLoad
 * @see WorldLoadCompleteEvent
 */
public interface WorldLoaderService {

  /**
   * Gets the loader service instance
   * @return Service implementation, or a no-op service if none exists
   */
  static WorldLoaderService worldLoader() {
    return BukkitServices.load(WorldLoaderService.class).orElse(NopService.NOP);
  }

  /**
   * Regenerates the world.
   * <p>
   * This will first unload and then delete all data for the specified {@code world}. Then it will
   * create a world with an identical world border and gamerules.
   *
   * @param world World to regenerate
   * @param seed Seed to use for the new world, if {@code null}, will generate a random seed
   *
   * @return Regenerated world's world load, or {@code null}, if the world failed to be recreated
   */
  @Nullable
  World remakeWorld(@NotNull World world, @Nullable Long seed);

  /**
   * Creates a world load for the specified {@code world}
   * <p>
   * The area the world load will generate is determined by the {@link org.bukkit.WorldBorder} of
   * the specified world
   *
   * @param world World to load
   * @return Load handle
   * @throws IllegalArgumentException If the world is already being loaded
   * @apiNote The loader will not start loading the world unless {@link WorldLoad#start()} is called
   */
  @NotNull
  WorldLoad loadWorld(@NotNull World world) throws IllegalArgumentException;

  /**
   * Gets a world load for a specified {@code world}
   *
   * @param world World to get the load handle for
   * @return World load, or {@code null}, if the specified {@code world} is not being loaded
   */
  @Nullable
  WorldLoad getLoadingWorld(@NotNull World world);

  /**
   * Stops the specified {@code world} from being loaded, this will trigger a
   * {@link WorldLoadCompleteEvent}, if the world is being loaded
   *
   * @param world World to stop loading
   * @return {@code true}, if the world was being loaded and the loading was stopped,
   *         {@code false}, if the world wasn't being loaded or couldn't be stopped from loading
   *
   */
  boolean stopLoading(@NotNull World world);

  /**
   * Test if the specified {@code world} is being loaded or not
   * @param world World to test
   * @return {@code true}, if the world is being loaded, {@code false}, otherwise
   */
  boolean isLoading(@NotNull World world);

  interface WorldLoad {

    /**
     * Sets the loading mode
     * @param mode Mode to use
     * @return {@code this}
     * @throws IllegalStateException If {@link #start()} has already been called
     */
    WorldLoad mode(@NotNull LoadMode mode) throws IllegalStateException;

    /**
     * Silences all logging output from this loader. Error messages will still be logged
     * @return {@code this}
     */
    WorldLoad silent();

    WorldLoad areaBounds(int minX, int minZ, int maxX, int maxZ);

    default WorldLoad area(int centerX, int centerZ, int radius) {
      return area(centerX, centerZ, radius, radius);
    }

    default WorldLoad area(int centerX, int centerZ, int radiusX, int radiusZ) {
      int minX = centerX - radiusX;
      int minZ = centerZ - radiusZ;
      int maxX = centerX + radiusX;
      int maxZ = centerZ + radiusZ;
      return areaBounds(minX, minZ, maxX, maxZ);
    }

    /**
     * Tests if the load is silent
     * @return {@code true}, if all logger output is muted, {@code false} otherwise
     */
    boolean isSilent();

    /**
     * Gets the world being loaded
     * @return World being loaded
     */
    World getWorld();

    /**
     * Starts the loader.
     * <p>
     * This future may not be the best way to listen to world loading completion. The server may
     * close while the loader is active and this callback will be disgregarded. A better alternative
     * may be the {@link WorldLoadCompleteEvent}
     *
     * @return Future that will be finished when the loader has finished loading
     * @throws IllegalStateException If {@link #start()} has been called already
     */
    CompletableFuture<World> start() throws IllegalStateException;

    /**
     * Stops the world from being loaded
     */
    void stop();
  }

  public enum LoadMode {
    /**
     * Load all chunks in parallel
     */
    ASYNC_PARALLEL,

    /**
     * Load all chunks async, after one another
     * <p>
     * Slower than {@link #ASYNC_PARALLEL}, but reduces the chance of the server becoming
     * overloaded with chunk loading
     */
    ASYNC_SERIES
  }
}

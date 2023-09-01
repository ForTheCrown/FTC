package net.forthecrown.worldloader.chunky;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.BukkitServices;
import net.forthecrown.worldloader.LoadingArea;
import net.forthecrown.worldloader.WorldLoadCompleteEvent;
import net.forthecrown.worldloader.WorldLoaderPlugin;
import net.forthecrown.worldloader.WorldLoaderService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;
import org.popcraft.chunky.iterator.PatternType;
import org.popcraft.chunky.shape.ShapeType;

public class ChunkyLoaderService implements WorldLoaderService {

  private static ChunkyLoaderService service;

  private final WorldLoaderPlugin plugin;
  private final ChunkyAPI chunky;

  private final Map<String, ChunkyLoad> map = new HashMap<>();

  private boolean registered;

  public ChunkyLoaderService(WorldLoaderPlugin plugin, ChunkyAPI chunky) {
    this.plugin = plugin;
    this.chunky = chunky;

    addListeners();
  }

  public static void register(WorldLoaderPlugin plugin) {
    if (service == null) {
      var api = ChunkyProvider.get().getApi();
      service = new ChunkyLoaderService(plugin, api);
    }

    service.registered = true;
    BukkitServices.register(WorldLoaderService.class, service);
  }

  public static void unregister() {
    if (service == null) {
      return;
    }

    service.registered = false;
    BukkitServices.unregister(WorldLoaderService.class, service);
  }

  public void addListeners() {
    chunky.onGenerationComplete(event -> {
      if (!registered) {
        return;
      }

      World world = Bukkit.getWorld(event.world());

      if (world == null) {
        return;
      }

      ChunkyLoad load = map.get(world.getName());

      if (load != null) {
        load.future.complete(world);
      }

      new WorldLoadCompleteEvent(world, true).callEvent();
    });
  }

  @Override
  public @Nullable World remakeWorld(@NotNull World world, @Nullable Long seed) {
    return plugin.getRemaker().remakeWorld(this, world, seed);
  }

  @Override
  public @NotNull WorldLoad loadWorld(@NotNull World world) throws IllegalArgumentException {
    return map.computeIfAbsent(world.getName(), s -> new ChunkyLoad(chunky, world));
  }

  @Override
  public @Nullable WorldLoad getLoadingWorld(@NotNull World world) {
    Objects.requireNonNull(world, "Null world");
    return map.get(world.getName());
  }

  @Override
  public boolean stopLoading(@NotNull World world) {
    Objects.requireNonNull(world, "Null world");
    return chunky.cancelTask(world.getName());
  }

  @Override
  public boolean isLoading(@NotNull World world) {
    Objects.requireNonNull(world, "Null world");
    return chunky.isRunning(world.getName()) || map.containsKey(world.getName());
  }

  static class ChunkyLoad implements WorldLoad {

    private final ChunkyAPI api;
    private final World world;

    private final CompletableFuture<World> future;

    private LoadingArea area;

    public ChunkyLoad(ChunkyAPI api, World world) {
      this.api = api;
      this.world = world;

      this.future = new CompletableFuture<>();
    }

    private void ensureNotLoading() {
      Preconditions.checkState(!api.isRunning(world.getName()), "World already loading");
    }


    @Override
    public WorldLoad mode(@NotNull LoadMode mode) throws IllegalStateException {
      Objects.requireNonNull(mode);
      ensureNotLoading();
      return this;
    }

    @Override
    public WorldLoad areaBounds(int minX, int minZ, int maxX, int maxZ) {
      ensureNotLoading();
      this.area = new LoadingArea(minX, minZ, maxX, maxZ);
      return this;
    }

    @Override
    public WorldLoad silent() {
      return this;
    }

    @Override
    public boolean isSilent() {
      return false;
    }

    @Override
    public World getWorld() {
      return world;
    }

    @Override
    public CompletableFuture<World> start() throws IllegalStateException {
      ensureNotLoading();

      var area = LoadingArea.getArea(this.area, world);
      double centerX = area.centerX();
      double centerZ = area.centerZ();

      double radiusX = area.sizeX() / 2.0d;
      double radiusZ = area.sizeZ() / 2.0d;

      api.startTask(
          world.getName(),
          ShapeType.SQUARE,
          centerX, centerZ,
          radiusX, radiusZ,
          PatternType.REGION
      );

      return future;
    }

    @Override
    public void stop() {
      api.cancelTask(world.getName());
    }
  }
}

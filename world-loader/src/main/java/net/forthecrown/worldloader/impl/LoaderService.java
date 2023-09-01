package net.forthecrown.worldloader.impl;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import lombok.Getter;
import net.forthecrown.worldloader.WorldLoaderPlugin;
import net.forthecrown.worldloader.WorldLoaderService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LoaderService implements WorldLoaderService {

  final WorldLoaderPlugin plugin;

  @Getter
  private final ExecutorService executor;

  @Getter
  private final Executor mainThreadExecutor;

  private Semaphore semaphore;

  private final Map<UUID, LoadingWorld> worlds = new HashMap<>();

  public LoaderService(WorldLoaderPlugin plugin) {
    this.plugin = plugin;

    this.executor = Executors.newCachedThreadPool(new LoaderThreadFactory());

    var scheduler = Bukkit.getScheduler();
    this.mainThreadExecutor = scheduler.getMainThreadExecutor(plugin);
  }

  public Semaphore getSemaphore() {
    if (semaphore != null) {
      return semaphore;
    }

    this.semaphore = new Semaphore(plugin.getLoaderConfig().maxChunksLoading);
    return semaphore;
  }

  @Override
  public World remakeWorld(@NotNull World world, @Nullable Long seed) {
    return plugin.getRemaker().remakeWorld(this, world, seed);
  }

  @Override
  public @NotNull LoadingWorld loadWorld(@NotNull World world) throws IllegalArgumentException {
    Preconditions.checkState(!isLoading(world), "World is already loading");

    LoadMode mode = plugin.getLoaderConfig().defaultLodeMode;
    LoadingWorld loading = new LoadingWorld(this, world, mode);

    worlds.put(world.getUID(), loading);

    return loading;
  }

  @Override
  public @Nullable LoadingWorld getLoadingWorld(@NotNull World world) {
    return worlds.get(world.getUID());
  }

  @Override
  public boolean stopLoading(@NotNull World world) {
    LoadingWorld loading = getLoadingWorld(world);

    if (loading == null) {
      return false;
    }

    loading.close(false);
    remove(loading);

    return true;
  }

  @Override
  public boolean isLoading(@NotNull World world) {
    return worlds.containsKey(world.getUID());
  }

  public void remove(LoadingWorld world) {
    worlds.remove(world.getWorld().getUID());
  }

  public void shutdown() {
    executor.shutdownNow();
    worlds.clear();
  }
}

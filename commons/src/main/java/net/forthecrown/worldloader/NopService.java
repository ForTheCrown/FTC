package net.forthecrown.worldloader;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class NopService implements WorldLoaderService {

  static final NopService NOP = new NopService();

  @Override
  public World remakeWorld(@NotNull World world, @Nullable Long seed) {
    Objects.requireNonNull(world);
    return null;
  }

  @Override
  public @NotNull WorldLoad loadWorld(@NotNull World world) throws IllegalArgumentException {
    Objects.requireNonNull(world);
    return new NopLoad(world);
  }

  @Override
  public @Nullable WorldLoad getLoadingWorld(@NotNull World world) {
    return null;
  }

  @Override
  public boolean stopLoading(@NotNull World world) {
    return false;
  }

  @Override
  public boolean isLoading(@NotNull World world) {
    return false;
  }

  private static class NopLoad implements WorldLoad {

    private final World world;
    private boolean silent;

    public NopLoad(World world) {
      this.world = world;
    }

    @Override
    public World getWorld() {
      return world;
    }

    @Override
    public WorldLoad mode(@NotNull LoadMode mode) throws IllegalStateException {
      Objects.requireNonNull(mode);
      return this;
    }

    @Override
    public WorldLoad areaBounds(int minX, int minZ, int maxX, int maxZ) {
      return this;
    }

    @Override
    public WorldLoad silent() {
      this.silent = true;
      return this;
    }

    @Override
    public boolean isSilent() {
      return silent;
    }

    @Override
    public void stop() {

    }

    @Override
    public CompletableFuture<World> start() {
      return CompletableFuture.completedFuture(world);
    }
  }
}

package net.forthecrown.worldloader;

import java.util.concurrent.CompletableFuture;
import org.bukkit.World;

public interface SeedGenerator {

  SeedGenerator KEEP = world -> {
    return CompletableFuture.completedFuture(world.getSeed());
  };

  SeedGenerator RANDOM = world -> {
    return CompletableFuture.completedFuture(null);
  };

  CompletableFuture<Long> generateSeed(World world);
}

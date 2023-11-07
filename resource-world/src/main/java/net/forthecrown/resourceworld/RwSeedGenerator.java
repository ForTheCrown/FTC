package net.forthecrown.resourceworld;

import java.util.concurrent.CompletableFuture;
import net.forthecrown.worldloader.SeedGenerator;
import org.bukkit.World;

public class RwSeedGenerator implements SeedGenerator {

  private final RwPlugin plugin;

  public RwSeedGenerator(RwPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public CompletableFuture<Long> generateSeed(World world) {
    return plugin.getResourceWorld().findSeed(world.getWorldBorder().getSize());
  }
}

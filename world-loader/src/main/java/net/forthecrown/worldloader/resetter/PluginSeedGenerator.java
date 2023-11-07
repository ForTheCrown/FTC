package net.forthecrown.worldloader.resetter;

import java.util.concurrent.CompletableFuture;
import net.forthecrown.BukkitServices;
import net.forthecrown.worldloader.SeedGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class PluginSeedGenerator implements SeedGenerator {

  private final String pluginName;
  private final SeedGenerator fallback;

  public PluginSeedGenerator(String pluginName, SeedGenerator fallback) {
    this.pluginName = pluginName;
    this.fallback = fallback;
  }

  @Override
  public CompletableFuture<Long> generateSeed(World world) {
    Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);

    if (plugin == null) {
      return fallback.generateSeed(world);
    }

    var opt = BukkitServices.load(SeedGenerator.class, plugin);
    if (opt.isEmpty()) {
      return fallback.generateSeed(world);
    }

    return opt.get().generateSeed(world);
  }
}

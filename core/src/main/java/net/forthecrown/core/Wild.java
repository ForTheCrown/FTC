package net.forthecrown.core;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.file.Path;
import java.util.Random;
import java.util.Set;
import net.forthecrown.Loggers;
import net.forthecrown.command.Exceptions;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

public class Wild {

  private static final Logger LOGGER = Loggers.getLogger();

  private final Path path;
  private final Random random;

  private final Set<Biome> bannedBiomes = new ObjectOpenHashSet<>();

  private int maxRange = 15000;
  private int maxAttempts = 1024;

  private boolean fallingWild = false;

  public Wild() {
    this.path = PathUtil.pluginPath("wild.toml");
    this.random = new Random();
  }

  public Location getWildLocation(Player player) throws CommandSyntaxException {
    if (!test(player)) {
      throw Exceptions.create("Cannot use /wild here");
    }

    Location found = findWild(player.getWorld());

    if (found == null) {
      throw Exceptions.create("Cannot find Wild location");
    }

    return found;
  }

  public Location findWild(World world) {
    int[] limitsX = { 0, 0 };
    int[] limitsZ = { 0, 0 };

    WorldBorder border = world.getWorldBorder();
    Location center = border.getCenter();

    double radius = border.getSize() / 2;
    Location min = center.clone().subtract(radius, 0, radius);
    Location max = center.clone().add(radius, 0, radius);

    limitsX[0] = Math.max(-maxRange, min.getBlockX());
    limitsX[1] = Math.min( maxRange, max.getBlockX());

    limitsZ[0] = Math.max(-maxRange, min.getBlockZ());
    limitsZ[1] = Math.min( maxRange, max.getBlockZ());

    int attempts = 0;

    while (true) {
      attempts++;

      if (attempts >= maxAttempts) {
        return null;
      }

      int x = random.nextInt(limitsX[0], limitsX[1]);
      int z = random.nextInt(limitsZ[0], limitsZ[1]);

      int y;

      // Y level used to validate biome
      int validationY;

      if (world.hasCeiling()) {
        y = 128;
        boolean lastPassable = false;

        while (y > 0) {
          boolean passable = isPassable(world, x, y, z);

          if (lastPassable && passable) {
            break;
          }

          lastPassable = passable;
          y--;
        }

        validationY = y;
      } else {
        validationY = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE);

        if (fallingWild) {
          y = validationY + 100;
        } else {
          y = validationY;
        }

        // Decrement to prevent biome testing error, if we didn't do this, you might get
        // teleported to an ocean biome, because the block above the water is a birch_forest
        // or something, but below the water blocks is ocean
        validationY--;
      }

      Biome biome = world.getBiome(x, validationY, z);

      if (bannedBiomes.contains(biome)) {
        continue;
      }

      if (!isPassable(world, x, y, z) || !isPassable(world, x, y + 1, z)) {
        continue;
      }

      double xPos = x + 0.5d;
      double yPos = y;
      double zPos = z + 0.5d;

      return new Location(world, xPos, yPos, zPos);
    }
  }

  boolean isPassable(World w, int x, int y, int z) {
    return w.getBlockAt(x, y, z).isPassable();
  }

  public boolean test(Player player) {
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionQuery query = container.createQuery();

    return query.testState(
        BukkitAdapter.adapt(player.getLocation()),
        WorldGuardPlugin.inst().wrapPlayer(player),
        CoreFlags.WILD_ALLOWED
    );
  }

  public void load() {
    bannedBiomes.clear();

    PluginJar.saveResources("wild.toml", path);
    SerializationHelper.readAsJson(path, this::load);
  }

  private void load(JsonWrapper json) {
    maxRange = json.getInt("spawn_range", 15_000);
    maxAttempts = json.getInt("max_attempts", 1024);

    bannedBiomes.addAll(json.getList("banned_biomes", e -> JsonUtils.readEnum(Biome.class, e)));
    fallingWild = json.getBool("falling_wild", false);
  }

  public boolean fallingWild() {
    return fallingWild;
  }

  private interface PlayerFilter {
    boolean test(Player player);
  }
}

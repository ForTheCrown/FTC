package net.forthecrown.dungeons;

import io.papermc.paper.entity.TeleportFlag.EntityState;
import java.nio.file.Path;
import net.forthecrown.Worlds;
import net.forthecrown.utils.io.PathUtil;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public final class DungeonWorld {

  private DungeonWorld() {
  }

  public static final String WORLD_NAME = "world_dungeons";

  public static World get() {
    return Bukkit.getWorld(WORLD_NAME);
  }

  public static World reset() {
    World world = get();

    if (world != null) {
      removeWorld(world);
    }

    return new WorldCreator(WORLD_NAME)
        .generator("VoidGen")
        .type(WorldType.FLAT)
        .keepSpawnLoaded(TriState.FALSE)
        .environment(World.Environment.NORMAL)
        .createWorld();
  }

  static void removeWorld(World world) {
    Location serverSpawn = Worlds.overworld().getSpawnLocation();

    world.getPlayers().forEach(player -> {
      player.teleport(serverSpawn, EntityState.RETAIN_PASSENGERS, EntityState.RETAIN_VEHICLE);
    });

    Bukkit.unloadWorld(world, false);
    Path worldDir = world.getWorldFolder().toPath();
    PathUtil.safeDelete(worldDir);
  }
}
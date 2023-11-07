package net.forthecrown.worldloader;

import java.util.Objects;
import java.util.Random;
import net.forthecrown.FtcServer;
import net.forthecrown.Worlds;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.Nullable;

public class WorldRemaker {

  private final Random seedgen;

  public WorldRemaker() {
    this.seedgen = new Random();
  }

  public World remakeWorld(WorldLoaderService service, World world, @Nullable Long seed) {
    if (!Bukkit.isPrimaryThread()) {
      throw new IllegalArgumentException("Async remakeWorld call");
    }

    Objects.requireNonNull(world, "Null world");

    if (service.isLoading(world)) {
      service.stopLoading(world);
    }

    return remake(world, seed);
  }

  private World remake(World world, @Nullable Long seed) {
    WorldResetEvent event = new WorldResetEvent(world);
    event.setSeed(seed);
    event.callEvent();

    seed = event.getSeed();

    GameRule<?>[] rules = GameRule.values();
    Object[] ruleValues = new Object[rules.length];
    for (int i = 0; i < rules.length; i++) {
      ruleValues[i] = world.getGameRuleValue(rules[i]);
    }

    WorldBorder border = world.getWorldBorder();
    Location borderCenter = border.getCenter();
    double borderSize = border.getSize();

    WorldCreator creator = WorldCreator.name(world.getName())
        .copy(world)
        .seed(Objects.requireNonNullElseGet(seed, seedgen::nextLong));

    // Teleport all players back to spawn
    FtcServer server = FtcServer.server();
    world.getPlayers().forEach(player -> {
      player.teleport(server.getServerSpawn());
    });

    if (!Worlds.desroyWorld(world)) {
      return null;
    }

    world = creator.createWorld();

    if (world == null) {
      return null;
    }

    border = world.getWorldBorder();
    border.setCenter(borderCenter);
    border.setSize(borderSize);

    for (int i = 0; i < rules.length; i++) {
      GameRule rule = rules[i];
      Object value = ruleValues[i];
      world.setGameRule(rule, value);
    }

    return world;
  }
}

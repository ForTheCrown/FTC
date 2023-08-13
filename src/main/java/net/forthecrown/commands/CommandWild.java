package net.forthecrown.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Collection;
import java.util.Random;
import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Worlds;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.EntitySelector;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CommandWild extends FtcCommand {

  public static final int MAX_RANGE = 12_500;

  public CommandWild() {
    super("wild");

    setPermission(Permissions.DEFAULT);
    setDescription("Puts you in the wild, only available in the Resource World");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Teleports either the command sender or a specified player
   * to a random coordinate.
   * Note: only peeps with ftc.admin perm can specify players
   *
   * Valid usages of command:
   * - /wild
   * - /wild <target selector>
   *
   * Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Teleports you into the wilderness.")
        .addInfo("This command only works at spawn or")
        .addInfo("in the Resource World");

    factory.usage("<entities> [<world>]")
        .setPermission(Permissions.ADMIN)
        .addInfo("Teleports a <player> into the wilderness")
        .addInfo("in a [world]. If [world] is not set, then")
        .addInfo("the <entities> is teleported in their own world");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        // /wild
        .executes(c -> {
          Player p = c.getSource().asPlayer();

          if (!test(p)) {
            return 0;
          }

          Cooldown.testAndThrow(p, getName(), 30 * 20);

          wildTP(p, p.getWorld());
          return 0;
        })

        // /wild <player>
        .then(argument("player", ArgumentTypes.entities())
            .requires(c -> c.hasPermission(Permissions.ADMIN))

            .executes(c -> wildEntities(c, false))

            // /wild <player> <world>
            .then(argument("world", ArgumentTypes.world())
                .requires(c -> c.hasPermission(Permissions.ADMIN))

                .executes(c -> wildEntities(c, true))
            )
        );
  }

  private int wildEntities(CommandContext<CommandSource> c, boolean worldSet)
      throws CommandSyntaxException
  {
    Collection<? extends Entity> players
        = c.getArgument("player", EntitySelector.class)
        .findEntities(c.getSource());

    World world = worldSet
        ? c.getArgument("world", World.class)
        : null;

    for (Entity p : players) {
      wildTP(p, world == null ? p.getWorld() : world);
    }

    return 0;
  }

  boolean test(Player p) {
    var hazelguard = hazelguardRegion(Worlds.overworld());

    if (p.getWorld().equals(Worlds.resource())) {
      return true;
    }

    if (!p.getWorld().equals(Worlds.overworld())
        || !hazelguard.contains(p)
    ) {
      p.sendMessage(Messages.WILD_TEST_FAIL_TEXT);
      return false;
    }

    return true;
  }

  Bounds3i hazelguardRegion(World world) {
    RegionManager manager = WorldGuard.getInstance()
        .getPlatform()
        .getRegionContainer()
        .get(BukkitAdapter.adapt(world));

    ProtectedRegion region = manager.getRegion("hazelguard");

    return Bounds3i.of(
        Vectors.from(region.getMinimumPoint()),
        Vectors.from(region.getMaximumPoint())
    );
  }

  public static void wildTP(Entity p, World world) {
    if (p instanceof LivingEntity) {
      ((LivingEntity) p).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 400, 1));
    }

    p.teleport(wildLocation(world));

    if (p instanceof Player) {
      ((Player) p).playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.MASTER,
          1, 1);
    }

    if (p.getWorld().getName().contains("world_resource")) {
      p.sendMessage(Messages.WILD_RW_TEXT);
    }
  }

  private static boolean isInvalid(Location bottom) {
    return isInvalidLocation(bottom) || isInvalidLocation(bottom.clone().add(0, 1, 0));
  }

  private static boolean isInvalidLocation(Location location) {
    Biome biome = location.getWorld()
        .getBiome(location.getBlockX(), location.getBlockY(), location.getBlockX());

    if (biome.key().value().contains("ocean")) {
      return true;
    }

    return location.getBlock().isSolid();
  }

  public static Location wildLocation(World world) {
    return wildLocation(world, Util.RANDOM);
  }

  private static Location wildLocation(World world, Random random) {
    var b = world.getWorldBorder();
    var center = b.getCenter();

    final int maxSize = (int) ((b.getSize() / 2) - 200);

    final int[] xBounds = {
        Math.max(center.getBlockX() - maxSize, -MAX_RANGE),
        Math.min(center.getBlockX() + maxSize,  MAX_RANGE)
    };

    final int[] zBounds = {
        Math.max(center.getBlockZ() - maxSize, -MAX_RANGE),
        Math.min(center.getBlockZ() + maxSize,  MAX_RANGE)
    };

    boolean changeY = world.getName().contains("nether");

    int x = rand(xBounds, random);
    int z = rand(zBounds, random);
    int y = changeY ? random.nextInt(30, 121) : 150;

    Location result = new Location(world, x, y, z);

    while (isInvalid(result)) {
      x = rand(xBounds, random);
      z = rand(zBounds, random);

      if (changeY) {
        y = random.nextInt(30, 121);
      }

      result = new Location(world, x, y, z);
    }

    return result.toCenterLocation();
  }

  private static int rand(int[] bounds, Random random) {
    return random.nextInt(bounds[0], bounds[1] + 1);
  }
}
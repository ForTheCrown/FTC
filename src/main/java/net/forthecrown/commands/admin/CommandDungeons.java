package net.forthecrown.commands.admin;

import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.boss.DungeonBoss;
import net.forthecrown.events.PunchingBags;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.utils.text.Text;
import org.bukkit.entity.Player;

public class CommandDungeons extends FtcCommand {

  private static final String bossArg = "boss";

  public CommandDungeons() {
    super("dungeons");

    setPermission(Permissions.CMD_DUNGEONS);
    setDescription("Admin command to manage the dungeons");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   *
   * Permissions used:
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("spawndummy [<location: x,y,z>]")
        .addInfo("Spawns a punching bag")
        .addInfo("if [location] is not set, spawns it")
        .addInfo("where you're standing");

    var prefixed = factory.withPrefix("debug");
    prefixed.usage("apples <boss>")
        .addInfo("Gives you a <boss>'s golden apple");

    prefixed = prefixed.withPrefix("<boss>");
    prefixed.usage("kill", "Kills a boss, if it's alive");

    prefixed.usage("attemptSpawn",
        "Attempts to spawn the boss, as if",
        "a normal player were using the spawn boss",
        "slime in a boss room"
    );

    prefixed.usage("spawn",
        "Spawns the boss, if it",
        "hasn't already been spawned"
    );
  }

  @Override
  protected void createCommand(BrigadierCommand command) {
    command
        .then(literal("spawndummy")
            .executes(c -> {
              Player player = c.getSource().asPlayer();
              PunchingBags.spawnDummy(player.getLocation());
              return 0;
            })
            .then(argument("location", PositionArgument.position())
                .executes(c -> {
                  PunchingBags.spawnDummy(PositionArgument.getLocation(c, "location"));
                  return 0;
                })
            )
        )

        .then(literal("debug")
            .then(literal("apples")
                .then(argument("boss", EnumArgument.of(BossItems.class))
                    .executes(c -> {
                      BossItems boss = c.getArgument("boss", BossItems.class);

                      Player player = c.getSource().asPlayer();
                      player.getInventory().addItem(boss.item());

                      c.getSource().sendAdmin("Giving " + Text.prettyEnumName(boss) + " apple");
                      return 0;
                    })
                )
            )

            .then(argument(bossArg, RegistryArguments.DUNGEON_BOSS)
                .then(literal("spawn")
                    .executes(c -> {
                      Holder<DungeonBoss> boss = c.getArgument(bossArg, Holder.class);

                      boss.getValue().spawn();
                      c.getSource().sendAdmin("Spawning boss");
                      return 0;
                    })
                )
                .then(literal("kill")
                    .executes(c -> {
                      Holder<DungeonBoss> holder = c.getArgument(bossArg, Holder.class);
                      var boss = holder.getValue();

                      if (!boss.isAlive()) {
                        throw Exceptions.BOSS_NOT_ALIVE;
                      }

                      boss.kill(false);
                      c.getSource().sendAdmin("Killing boss");
                      return 0;
                    })
                )
                .then(literal("attemptSpawn")
                    .executes(c -> {
                      Player player = c.getSource().asPlayer();
                      Holder<DungeonBoss> holder = c.getArgument(bossArg, Holder.class);
                      var boss = holder.getValue();

                      boss.attemptSpawn(player);
                      c.getSource().sendAdmin("Attempting boss spawn");
                      return 0;
                    })
                )
            )
        );
  }
}
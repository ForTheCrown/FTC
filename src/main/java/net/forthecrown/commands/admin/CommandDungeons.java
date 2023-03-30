package net.forthecrown.commands.admin;

import static net.kyori.adventure.text.Component.text;

import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.boss.DungeonBoss;
import net.forthecrown.dungeons.boss.SpawnTest;
import net.forthecrown.dungeons.boss.SpawnTest.Items;
import net.forthecrown.events.PunchingBags;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.nbt.string.Snbt;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;

@SuppressWarnings("unchecked")
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
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("spawndummy")
            .executes(c -> {
              Player player = c.getSource().asPlayer();
              PunchingBags.spawnDummy(player.getLocation());
              return 0;
            })
            .then(argument("location", ArgumentTypes.position())
                .executes(c -> {
                  PunchingBags.spawnDummy(ArgumentTypes.getLocation(c, "location"));
                  return 0;
                })
            )
        )

        .then(literal("debug")
            .then(literal("apples")
                .then(argument("boss", ArgumentTypes.enumType(BossItems.class))
                    .executes(c -> {
                      BossItems boss = c.getArgument("boss", BossItems.class);

                      Player player = c.getSource().asPlayer();
                      player.getInventory().addItem(boss.item());

                      c.getSource().sendSuccess(text("Giving " + Text.prettyEnumName(boss) + " apple"));
                      return 0;
                    })
                )
            )

            .then(argument(bossArg, RegistryArguments.DUNGEON_BOSS)
                .then(literal("show_items")
                    .executes(c -> {
                      Holder<DungeonBoss> boss
                          = c.getArgument(bossArg, Holder.class);

                      var test = boss.getValue().getSpawnRequirement();

                      if (!(test instanceof Items)) {
                        throw Exceptions.format(
                            "Boss {0} doesn't have an item test",
                            boss.getKey()
                        );
                      }

                      SpawnTest.Items items
                          = (Items) boss.getValue().getSpawnRequirement();

                      var it = items.getItems().iterator();

                      TextJoiner joiner = TextJoiner.onNewLine()
                          .setPrefix(Text.renderString("Items: [\n"))
                          .setSuffix(Component.text("\n]"));

                      while (it.hasNext()) {
                        var n = it.next();
                        var tag = ItemStacks.save(n);

                        joiner.add(
                            Text.format("  - {0, item} [Click to copy NBT]", n)
                                .hoverEvent(Text.displayTag(tag, true))
                                .clickEvent(
                                    ClickEvent.copyToClipboard(
                                        Snbt.toString(tag)
                                    )
                                )
                        );
                      }

                      c.getSource().sendMessage(joiner.asComponent());
                      return 0;
                    })
                )

                .then(literal("spawn")
                    .executes(c -> {
                      Holder<DungeonBoss> boss = c.getArgument(bossArg, Holder.class);

                      boss.getValue().spawn();
                      c.getSource().sendSuccess(text("Spawning boss"));
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
                      c.getSource().sendSuccess(text("Killing boss"));
                      return 0;
                    })
                )
                .then(literal("attemptSpawn")
                    .executes(c -> {
                      Player player = c.getSource().asPlayer();
                      Holder<DungeonBoss> holder = c.getArgument(bossArg, Holder.class);
                      var boss = holder.getValue();

                      boss.attemptSpawn(player);
                      c.getSource().sendSuccess(text("Attempting boss spawn"));
                      return 0;
                    })
                )
            )
        );
  }
}
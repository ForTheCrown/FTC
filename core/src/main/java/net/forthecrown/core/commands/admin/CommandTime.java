package net.forthecrown.core.commands.admin;

import static net.forthecrown.McConstants.TICKS_PER_DAY;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.World;

public class CommandTime extends FtcCommand {

  public CommandTime() {
    super("time");

    setDescription("Sets the time in your world");
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /time add <amount>
   * /time set <amount>
   * /time query
   *
   * Permissions used:
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("set <day | noon | night | midnight>")
        .addInfo("Sets the time in your world to a ")
        .addInfo("corresponding constant");

    factory.usage("set <time: world time>")
        .addInfo("Sets the world time");

    factory.usage("add <time: world time>")
        .addInfo("Adds to <time> the world's time");

    factory.usage("get [<world>]")
        .addInfo("Gets the current world time in [world].")
        .addInfo("If [world] is not set, gets the time in")
        .addInfo("the world you're in");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("set")

            .then(timeArg("day", 1000))
            .then(timeArg("noon", 6000))
            .then(timeArg("night", 13000))
            .then(timeArg("midnight", 18000))

            .then(argument("time", Arguments.GAMETIME)
                .executes(c -> timeThing(c, false))
            )
        )

        .then(literal("add")
            .then(argument("time", Arguments.GAMETIME)
                .executes(c -> timeThing(c, true))
            )
        )

        .then(literal("get")
            .then(argument("world", ArgumentTypes.world())
                .executes(c -> {
                  World world = c.getArgument("world", World.class);
                  return timeInfo(c.getSource(), world);
                })
            )

            .executes(c -> {
              World world = c.getSource().getWorld();
              return timeInfo(c.getSource(), world);
            })
        );
  }

  private int timeInfo(CommandSource source, World world) {
    long fullTime = world.getFullTime();
    long day = fullTime / 1000 / 24;
    long year = day / 365;

    source.sendSuccess(
        Text.format(
            """
                World times:
                Full time: {0}
                Time: {1}
                Day: {2}
                Year: {3}
                """,
            fullTime, world.getTime(), day, year
        )
    );
    return 0;
  }

  private LiteralArgumentBuilder<CommandSource> timeArg(String name, int time) {
    return literal(name).executes(c -> {
      World world = c.getSource().getWorld();
      long timeAdd = world.getFullTime() - (world.getFullTime() % TICKS_PER_DAY);

      return setTime(c.getSource(), time + timeAdd);
    });
  }

  private int timeThing(CommandContext<CommandSource> c, boolean add) {
    int time = c.getArgument("time", Integer.class);
    World world = c.getSource().getWorld();

    long actualTime = time + (add ? world.getFullTime() : 0);
    return setTime(c.getSource(), actualTime);
  }

  private int setTime(CommandSource source, long time) {
    World world = source.getWorld();

    world.setFullTime(time);

    source.sendSuccess(
        Component.text("Set time of ")
            .append(Component.text(world.getName()))
            .append(Component.text(" to " + time))
    );
    return 0;
  }
}
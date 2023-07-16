package net.forthecrown.core.commands.admin;

import static net.forthecrown.McConstants.TICKS_PER_DAY;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.Permissions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CommandPlayerTime extends FtcCommand {

  public CommandPlayerTime() {
    super("playertime");

    setAliases("ptime");
    setPermission(Permissions.ADMIN);

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.ONLINE_USER)
            .executes(c -> {
              Player player = get(c);
              long time = player.getPlayerTime();
              long days = time / 1000 / 24;

              c.getSource().sendSuccess(
                  text(player.getName() + "'s time: " + days + " days, absolute time: " + time)
              );
              return 0;
            })

            .then(literal("reset")
                .executes(c -> {
                  Player player = get(c);

                  player.resetPlayerTime();

                  c.getSource().sendSuccess(text("Reset " + player.getName() + "'s time"));
                  return 0;
                })
            )

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
        );
  }

  LiteralArgumentBuilder<CommandSource> timeArg(String name, long multiplier) {
    return literal(name)
        .executes(c -> {
          Player player = get(c);
          long timeAdd = player.getWorld().getFullTime() - (player.getPlayerTime() % TICKS_PER_DAY);
          long time = timeAdd + multiplier;

          return setTime(c.getSource(), player, time);
        });
  }

  int timeThing(CommandContext<CommandSource> c, boolean add) throws CommandSyntaxException {
    int time = c.getArgument("time", Integer.class);
    World world = c.getSource().getWorld();
    Player player = get(c);

    long actualTime = time + (add ? world.getFullTime() : 0);
    return setTime(c.getSource(), player, actualTime);
  }

  int setTime(CommandSource source, Player player, long time) {
    player.setPlayerTime(time, false);

    source.sendSuccess(
        text("Set time of ")
            .append(player.displayName())
            .append(text(" to " + time))
    );
    return 0;
  }

  Player get(CommandContext<CommandSource> c) throws CommandSyntaxException {
    return Arguments.getUser(c, "user").getPlayer();
  }
}
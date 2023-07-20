package net.forthecrown.core.commands;

import com.google.common.collect.Collections2;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;

public class CommandNear extends FtcCommand {

  public static final int DEFAULT_DISTANCE = 200;

  public CommandNear() {
    super("near");

    setAliases("nearby");
    setPermission(CorePermissions.NEARBY);
    setDescription("Shows nearby players");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory = factory.withPermission(CorePermissions.NEARBY_ADMIN);

    factory.usage("<radius: number(1..100,000)>")
        .addInfo("Shows all players with a <radius>");

    factory.usage("<user> [<radius: number(1..100,000)>]")
        .addInfo("Shows all players near to a <user>")
        .addInfo("and within an optional [range]");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          User user = getUserSender(c);
          return showNearby(user.getLocation(), DEFAULT_DISTANCE, c.getSource());
        })

        .then(argument("radius", IntegerArgumentType.integer(1, 100000))
            .requires(s -> s.hasPermission(CorePermissions.NEARBY_ADMIN))

            .executes(c -> {
              User user = getUserSender(c);
              return showNearby(user.getLocation(), c.getArgument("radius", Integer.class),
                  c.getSource());
            })
        )

        .then(argument("user", Arguments.ONLINE_USER)
            .requires(s -> s.hasPermission(CorePermissions.NEARBY_ADMIN))

            .executes(c -> {
              User user = Arguments.getUser(c, "user");
              return showNearby(user.getLocation(), DEFAULT_DISTANCE, c.getSource());
            })

            .then(argument("radius", IntegerArgumentType.integer(1, 100000))
                .requires(s -> s.hasPermission(CorePermissions.NEARBY_ADMIN))

                .executes(c -> {
                  User user = Arguments.getUser(c, "user");
                  int radius = c.getArgument("radius", Integer.class);

                  return showNearby(user.getLocation(), radius, c.getSource());
                })
            )
        );
  }

  private int showNearby(Location loc, int radius, CommandSource source)
      throws CommandSyntaxException
  {
    var players = Collections2.transform(loc.getNearbyPlayers(radius), Users::get);

    players.removeIf(user -> user.hasPermission(CorePermissions.NEARBY_IGNORE)
        || user.getGameMode() == GameMode.SPECTATOR
        || user.getName().equalsIgnoreCase(source.textName())
        || user.get(Properties.PROFILE_PRIVATE)
        || user.get(Properties.VANISHED)
    );

    if (players.isEmpty()) {
      throw CoreExceptions.NO_NEARBY_PLAYERS;
    }

    TextComponent.Builder builder = Component.text()
        .append(CoreMessages.NEARBY_HEADER);

    builder.append(
        TextJoiner.onComma().add(
                players.stream()
                    .map(user -> Text.format("{0, user} &7({1})",
                        NamedTextColor.YELLOW,
                        user,
                        dist(user.getLocation(), loc)
                    ))
            )
            .asComponent()
    );

    source.sendMessage(builder.build());
    return 0;
  }

  private String dist(Location from, Location to) {
    return Math.floor(from.distance(to)) + "b";
  }
}
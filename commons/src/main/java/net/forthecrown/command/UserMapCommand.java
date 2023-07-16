package net.forthecrown.command;

import static net.forthecrown.text.Text.format;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import java.util.UUID;
import net.forthecrown.Permissions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Messages;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.user.User;
import net.forthecrown.utils.ScoreIntMap;
import net.kyori.adventure.text.Component;

public class UserMapCommand extends FtcCommand {

  private final ScoreIntMap<UUID> map;
  private final Long2ObjectFunction<Component> displayProvider;
  private final String units;

  public UserMapCommand(
      String name,
      String units,
      ScoreIntMap<UUID> map,
      Long2ObjectFunction<Component> displayProvider,
      String... aliases
  ) {
    super(name);

    this.map = map;
    this.displayProvider = displayProvider;
    this.units = units;

    setAliases(aliases);
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /<name>
   * /<name> <user>
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Shows you your " + units);

    factory.usage("<player>")
        .addInfo("Shows you <player>'s " + units);

    factory.usage("<player> add <amount: number>")
        .setCondition(source -> source.hasPermission(Permissions.ADMIN))
        .addInfo("Adds <amount> of %s to <player>", units);

    factory.usage("<player> set <amount: number>")
        .setCondition(source -> source.hasPermission(Permissions.ADMIN))
        .addInfo("Sets the %s of <player> to <amount>", units);

    factory.usage("<player> remove <amount: number>")
        .setCondition(source -> source.hasPermission(Permissions.ADMIN))
        .addInfo("Removes <amount> from the %s of <player>", units);

    factory.usage("<player> delete")
        .setCondition(source -> source.hasPermission(Permissions.ADMIN))
        .addInfo("Deletes the <player>'s %s data", units);
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        // /<command>
        .executes(c -> lookup(c.getSource(), getUserSender(c)))

        // /<command> <user>
        .then(argument("user", Arguments.USER)
            .executes(c -> {
              var user = Arguments.getUser(c, "user");
              return lookup(c.getSource(), user);
            })

            // /<command> <user> set <amount>
            .then(literal("set")
                .requires(source -> source.hasPermission(Permissions.ADMIN))

                .then(argument("amount", IntegerArgumentType.integer(0))
                    .executes(c -> {
                      var user = Arguments.getUser(c, "user");
                      int amount = c.getArgument("amount", Integer.class);

                      map.set(user.getUniqueId(), amount);
                      int newAmount = map.get(user.getUniqueId());

                      c.getSource().sendSuccess(
                          format("Set {0, user}'s {1} to {2}",
                              user,
                              getName(),
                              displayProvider.apply(newAmount)
                          )
                      );
                      return 0;
                    })
                )
            )

            // /<command> <user> add <amount>
            .then(literal("add")
                .requires(source -> source.hasPermission(Permissions.ADMIN))

                .then(argument("amount", IntegerArgumentType.integer(1))
                    .executes(c -> {
                      var user = Arguments.getUser(c, "user");
                      int amount = c.getArgument("amount", Integer.class);

                      map.add(user.getUniqueId(), amount);
                      int newAmount = map.get(user.getUniqueId());

                      c.getSource().sendSuccess(
                          format("Added {0} to {1, user}'s {2}, new value: {3}",
                              displayProvider.apply(amount),
                              user,
                              getName(),
                              displayProvider.apply(newAmount)
                          )
                      );
                      return 0;
                    })
                )
            )

            // /<command> <user> subtract <amount>
            .then(literal("subtract")
                .requires(source -> source.hasPermission(Permissions.ADMIN))

                .then(argument("amount", IntegerArgumentType.integer(1))
                    .executes(c -> {
                      var user = Arguments.getUser(c, "user");
                      int amount = c.getArgument("amount", Integer.class);

                      map.remove(user.getUniqueId(), amount);
                      int newAmount = map.get(user.getUniqueId());

                      c.getSource().sendSuccess(
                          format("Subtracted {0} from {1, user}'s {2}, new value: {3}",
                              displayProvider.apply(amount),
                              user,
                              getName(),
                              displayProvider.apply(newAmount)
                          )
                      );
                      return 0;
                    })
                )
            )

            // /<command> <user> remove
            // This will remove the given user from the map
            // completely, as opposed to the subtract argument,
            // which simply decrements the given user's value
            // in the map
            .then(literal("delete")
                .requires(source -> source.hasPermission(Permissions.ADMIN))

                .executes(c -> {
                  var user = Arguments.getUser(c, "user");
                  map.remove(user.getUniqueId());

                  c.getSource().sendSuccess(
                      format("Removed {0, user} from {1} list", user, getName())
                  );
                  return 0;
                })
            )
        );
  }

  private int lookup(CommandSource source, User user) {
    var val = map.get(user.getUniqueId());
    var self = source.textName().equals(user.getName());

    if (self) {
      source.sendMessage(
          Messages.unitQuerySelf(displayProvider.apply(val))
      );
    } else {
      source.sendMessage(
          Messages.unitQueryOther(
              displayProvider.get(val),
              user
          )
      );
    }

    return 0;
  }
}
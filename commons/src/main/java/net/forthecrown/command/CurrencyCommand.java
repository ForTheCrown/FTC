package net.forthecrown.command;

import static net.forthecrown.text.Text.format;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.Permissions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Messages;
import net.forthecrown.user.currency.Currency;
import net.forthecrown.user.User;
import net.kyori.adventure.text.format.NamedTextColor;

public class CurrencyCommand extends FtcCommand {

  private final Currency currency;

  public CurrencyCommand(
      String name,
      Currency currency,
      String... aliases
  ) {
    super(name);

    this.currency = currency;

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
    String units = currency.name();

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

                      currency.set(user.getUniqueId(), amount);
                      int newAmount = currency.get(user.getUniqueId());

                      c.getSource().sendSuccess(
                          format("Set &e{0, user}&r's {1} to &6{2}&r.",
                              NamedTextColor.GRAY,
                              user,
                              getName(),
                              currency.format(newAmount)
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

                      currency.add(user.getUniqueId(), amount);
                      int newAmount = currency.get(user.getUniqueId());

                      c.getSource().sendSuccess(
                          format("Added &e{0}&r to &6{1, user}&r's {2}, new value: &e{3}&r.",
                              NamedTextColor.GRAY,
                              currency.format(amount),
                              user,
                              getName(),
                              currency.format(newAmount)
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

                      currency.remove(user.getUniqueId(), amount);
                      int newAmount = currency.get(user.getUniqueId());

                      c.getSource().sendSuccess(
                          format("Subtracted &e{0}&r from &6{1, user}&r's {2}, new value: &e{3}&r.",
                              NamedTextColor.GRAY,
                              currency.format(amount),
                              user,
                              getName(),
                              currency.format(newAmount)
                          )
                      );
                      return 0;
                    })
                )
            )
        );
  }

  private int lookup(CommandSource source, User user) {
    var val = currency.get(user.getUniqueId());
    var self = source.textName().equals(user.getName());

    if (self) {
      source.sendMessage(Messages.unitQuerySelf(currency.format(val)));
    } else {
      source.sendMessage(Messages.unitQueryOther(currency.format(val), user));
    }

    return 0;
  }
}
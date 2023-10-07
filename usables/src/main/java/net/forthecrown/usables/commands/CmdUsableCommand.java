package net.forthecrown.usables.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import lombok.Getter;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.usables.CmdUsables;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.conditions.TestPermission;
import net.forthecrown.usables.objects.CommandUsable;
import net.forthecrown.user.User;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class CmdUsableCommand<T extends CommandUsable> extends InteractableCommand<T> {

  protected final CmdUsables<T> usables;
  protected final Class<T> typeClass;
  protected final String displayName;

  @Getter
  protected final ArgumentType<T> argumentType;

  public CmdUsableCommand(
      String name,
      CmdUsables<T> usables,
      Class<T> typeClass
  ) {
    super(name, name);

    this.usables = usables;
    this.displayName = Text.capitalizeFully(name);
    this.typeClass = typeClass;

    argumentType = new CmdUsableArgument<>(usables, displayName);

    generateUseArgument = false;
  }

  @Override
  protected void createPrefixedUsages(UsageFactory factory) {
    factory.usage("").addInfo("Displays a list of %ss", displayName);
  }

  @Override
  protected void createUsages(UsageFactory factory) {
    factory.usage("").addInfo("Uses a %s", displayName);

    super.createUsages(factory.withPermission(getAdminPermission()));
  }

  @Override
  protected <B extends ArgumentBuilder<CommandSource, B>> void createEditArguments(
      B argument,
      UsableProvider<T> provider
  ) {
    argument.executes(c -> {
      var t = provider.get(c);

      var player = c.getSource().asPlayer();
      t.interact(player);

      return 0;
    });

    argument.then(argument("selector", Arguments.ONLINE_USERS)
        .requires(hasAdminPermission())

        .executes(c -> {
          T value = provider.get(c);
          List<User> users = Arguments.getUsers(c, "selector");

          int successes = 0;

          for (User user : users) {
            var interaction = value.createInteraction(user.getPlayer(), true);

            if (value.interact(interaction)) {
              successes++;
            }
          }

          if (successes < 1) {
            throw Exceptions.format("Failed to make any players interact with {0}",
                value.displayName()
            );
          }

          if (users.size() == 1) {
            c.getSource().sendSuccess(
                Text.format("Made &e{0, user}&r interact with &6{1}&r.", NamedTextColor.GRAY,
                    users.get(0), value.displayName()
                )
            );
          } else {
            c.getSource().sendSuccess(
                Text.format("Made &e{0, number} players&r interact with &6{1}&r.",
                    NamedTextColor.GRAY,
                    users.size(), value.displayName()
                )
            );
          }

          return 0;
        })
    );

    argument.then(literal("remove")
        .requires(hasAdminPermission())

        .executes(c -> {
          T value = provider.get(c);
          usables.remove(value);

          c.getSource().sendSuccess(
              Text.format("Removed {0} named '&e{1}&r'", NamedTextColor.GRAY,
                  displayName, value.displayName()
              )
          );
          return 0;
        })
    );

    super.createEditArguments(argument, provider);
  }

  @Override
  protected void addPrefixedArguments(LiteralArgumentBuilder<CommandSource> builder) {
    builder.executes(c -> {
      var user = getUserSender(c);
      var list = usables.getUsable(user.getPlayer());

      if (list.isEmpty()) {
        throw Exceptions.NOTHING_TO_LIST;
      }

      user.sendMessage(
          Text.format("{0}s ({1, number}): &e{2}",
              NamedTextColor.GRAY,

              displayName,
              list.size(),
              TextJoiner.onComma().add(list.stream().map(CommandUsable::displayName))
          )
      );
      return 0;
    });

    builder.then(literal("create")
        .requires(hasAdminPermission())

        .then(argument("name", Arguments.FTC_KEY)
            .executes(c -> {
              String name = c.getArgument("name", String.class);

              if (usables.contains(name)) {
                throw Exceptions.format("Name '{0}' is already in use", name);
              }

              T created = create(name, c.getSource());

              Condition condition = new TestPermission(getPermission() + "." + name);
              created.getConditions().addLast(condition);

              usables.add(created);

              c.getSource().sendSuccess(
                  Text.format("Created {0} named '&e{1}&r'", NamedTextColor.GRAY,
                      displayName, created.displayName()
                  )
              );

              return 0;
            })
        )
    );
  }

  protected abstract T create(String name, CommandSource source) throws CommandSyntaxException;

  @Override
  protected UsableProvider<T> getProvider(String argument) {
    return context -> context.getArgument(argument, typeClass);
  }
}

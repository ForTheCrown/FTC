package net.forthecrown.commands.admin;

import static net.forthecrown.core.Cooldowns.NO_END_COOLDOWN;
import static net.forthecrown.core.Cooldowns.TRANSIENT_CATEGORY;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Cooldowns;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandCooldown extends FtcCommand {

  public CommandCooldown() {
    super("Cooldown");

    setDescription("Controls various cooldowns");
    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    var prefixed = factory.withPrefix("<user> <category>");

    prefixed.usage("")
        .addInfo("Shows how long a <user> is on cooldown in a <category>");

    prefixed.usage("add [<length>]")
        .addInfo("Adds a <user> into a <category>'s cooldown.")
        .addInfo("If <length> is not set, the cooldown will never end");

    prefixed.usage("remove")
        .addInfo("Removes a <user> from a cooldown in a <category>");
  }

  @Override
  protected void createCommand(BrigadierCommand command) {
    command.then(argument("user", Arguments.USER)
        .then(argument("category", StringArgumentType.string())
            .suggests((context, builder) -> {
              var input = builder.getRemainingLowerCase();

              if (CompletionProvider.startsWith(input, TRANSIENT_CATEGORY)) {
                builder.suggest(
                    TRANSIENT_CATEGORY,
                    toTooltip("Transient category that does NOT get saved")
                );
              }

              return CompletionProvider.suggestMatching(
                  builder,
                  Cooldowns.getCooldowns().getExistingCategories()
              );
            })


            // Test if on cooldown, show remaining duration
            .executes(this::showData)

            .then(literal("add")
                .executes(c -> add(c, true))

                .then(argument("length", TimeArgument.time())
                    .executes(c -> add(c, false))
                )
            )

            .then(literal("remove")
                .executes(this::remove)
            )
        )
    );
  }

  private int showData(CommandContext<CommandSource> c)
      throws CommandSyntaxException {
    User user = Arguments.getUser(c, "user");
    String category = c.getArgument("category", String.class);
    var cds = Cooldowns.getCooldowns();

    Duration duration = cds.getRemainingTime(user.getUniqueId(), category);

    if (duration == null) {
      throw Exceptions.format("{0, user} is not on '{1}' cooldown",
          user, category
      );
    }

    if (duration.getSeconds() < 0) {
      c.getSource().sendMessage(
          Text.format("&e{0, user}&r's '&e{1}&r' cooldown will never end",
              NamedTextColor.GRAY,
              user, category
          )
      );
    } else {
      c.getSource().sendMessage(
          Text.format(
              "&e{0, user}&r '&e{1}&r' cooldown "
                  + "remaining: &e{2, time, -short -biggest}",

              NamedTextColor.GRAY,
              user, category, duration
          )
      );
    }

    return 0;
  }

  private int add(CommandContext<CommandSource> c, boolean neverEnding)
      throws CommandSyntaxException {
    User user = Arguments.getUser(c, "user");
    String category = c.getArgument("category", String.class);

    long length;

    if (neverEnding) {
      length = NO_END_COOLDOWN;
    } else {
      length = TimeArgument.getMillis(c, "length");
    }

    var cds = Cooldowns.getCooldowns();

    if (cds.isOnCooldown(user.getUniqueId(), category)) {
      throw Exceptions.format("{0, user} is already on cooldown for {1}",
          user, category
      );
    }

    cds.cooldown(user.getUniqueId(), category, length);

    c.getSource().sendAdmin(
        Text.format(
            "Placed &e{0, user}&r into cooldown, "
                + "category='&e{1}&r', "
                + "length=&e{2, time, -short -biggest}&r",
            NamedTextColor.GRAY,

            user, category,
            length == NO_END_COOLDOWN ? "Eternal" : length
        )
    );
    return 0;
  }

  private int remove(CommandContext<CommandSource> c)
      throws CommandSyntaxException {
    User user = Arguments.getUser(c, "user");
    String category = c.getArgument("category", String.class);
    var cds = Cooldowns.getCooldowns();

    if (!cds.remove(user.getUniqueId(), category)) {
      throw Exceptions.format("{0, user} is not on cooldown for {1}",
          user, category
      );
    }

    c.getSource().sendAdmin(
        Text.format("&e{0, user}&r was removed from the '&e{1}&r' cooldown",
            NamedTextColor.GRAY,
            user, category
        )
    );
    return 0;
  }
}
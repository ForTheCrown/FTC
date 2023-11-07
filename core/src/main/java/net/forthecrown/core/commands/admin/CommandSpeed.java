package net.forthecrown.core.commands.admin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandSpeed extends FtcCommand {

  public static float WALK_BASE_SPEED = 0.2f;
  public static float FLY_BASE_SPEED  = 0.1f;

  static final double MIN = -10.0D;
  static final double MAX =  10.0D;

  public CommandSpeed() {
    super("speed");
    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    addUsages(factory, "walk");
    addUsages(factory, "fly");
  }

  private void addUsages(UsageFactory factory, String cat) {
    factory = factory.withPrefix(cat);

    factory.usage("query")
        .addInfo("Queries your %sing speed", cat);

    factory.usage("query <player>")
        .addInfo("Queries a <player>'s %sing speed", cat);

    factory.usage("<value: number(-10, 10)>")
        .addInfo("Sets your %sing speed", cat);

    factory.usage("<value: number(-10, 10)> <player>")
        .addInfo("Sets a <player>'s %sing speed", cat);
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(arg(true))
        .then(arg(false));
  }

  private LiteralArgumentBuilder<CommandSource> arg(boolean fly) {
    return literal(fly ? "fly" : "walk")
        .then(argument("value", new SpeedArgument(fly ? FLY_BASE_SPEED : WALK_BASE_SPEED))
            .executes(c -> changeSpeed(
                getUserSender(c),
                c.getArgument("value", Speed.class),
                c.getSource(),
                fly
            ))

            .then(argument("user", Arguments.ONLINE_USER)
                .executes(c -> changeSpeed(
                    Arguments.getUser(c, "user"),
                    c.getArgument("value", Speed.class),
                    c.getSource(),
                    fly
                ))
            )
        )

        .then(literal("query")
            .executes(c -> querySpeed(getUserSender(c), c.getSource(), fly))

            .then(argument("user", Arguments.ONLINE_USER)
                .executes(c -> querySpeed(
                    Arguments.getUser(c, "user"),
                    c.getSource(),
                    fly
                ))
            )
        );
  }

  private int changeSpeed(User user,
                          Speed amount,
                          CommandSource source,
                          boolean fly
  ) {
    var player = user.getPlayer();

    if (fly) {
      player.setFlySpeed((float) amount.actual());
    } else {
      player.setWalkSpeed((float) amount.actual());
    }

    source.sendSuccess(
        Text.format(
            "Set &e{0, user}&r's {1}ing speed to &e{2, number}&r",
            NamedTextColor.GRAY,
            user,
            fly ? "fly" : "walk",
            amount.display()
        )
    );
    return 0;
  }

  private int querySpeed(User user, CommandSource source, boolean fly) {
    Speed speed;
    var player = user.getPlayer();

    if (fly) {
      speed = Speed.fromActualValue(player.getFlySpeed(), FLY_BASE_SPEED);
    } else {
      speed = Speed.fromActualValue(player.getWalkSpeed(), WALK_BASE_SPEED);
    }

    source.sendSuccess(
        Text.format(
            "&e{0, user}&r's {1}ing speed is &e{2, number}&r"
                + " (internal value: {3, number})",
            NamedTextColor.GRAY,

            user,
            fly ? "fly" : "walk",
            speed.display(),
            speed.actual()
        ),

        // Do not broadcast
        false
    );

    return 0;
  }

  record Speed(double display, double actual) {

    static Speed fromDisplayValue(double v, double baseValue) {
      if (v == 1) {
        return new Speed(1, baseValue);
      }

      double value = v / MAX;
      return new Speed(v, value);
    }

    static Speed fromActualValue(double value, double baseValue) {
      if (value == baseValue) {
        return new Speed(1, baseValue);
      }

      double display = (value * MAX);
      return new Speed(display, value);
    }
  }

  static class SpeedArgument implements ArgumentType<Speed> {

    static final DoubleArgumentType DOUBLE_ARG
        = DoubleArgumentType.doubleArg(MIN, MAX);

    private final double baseValue;

    public SpeedArgument(double baseValue) {
      this.baseValue = baseValue;
    }

    @Override
    public Speed parse(StringReader reader) throws CommandSyntaxException {
      if (Readers.startsWithIgnoreCase(reader, "reset")) {
        reader.readUnquotedString();
        return new Speed(1, baseValue);
      }

      double value = DOUBLE_ARG.parse(reader);
      return Speed.fromDisplayValue(value, baseValue);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
        CommandContext<S> context,
        SuggestionsBuilder builder
    ) {
      return Completions.suggest(
          builder,
          "reset", "1", "2.5", "5", "7.5", "10"
      );
    }
  }
}
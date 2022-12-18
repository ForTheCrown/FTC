package net.forthecrown.commands.guild;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import java.time.Duration;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.UserParseResult;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.guilds.GuildConfig;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;

public class GuildMultiplierNode extends GuildCommandNode {
  private static final Argument<Long> TIME_ARG
      = Argument.builder("length", TimeArgument.time()).build();

  private static final Argument<Float> VALUE
      = Argument.builder("value", FloatArgumentType.floatArg()).build();

  private static final Argument<UserParseResult> USER
      = Argument.builder("player", Arguments.USER).build();

  private static final ArgsArgument ARGS = ArgsArgument.builder()
      .addRequired(TIME_ARG)
      .addRequired(VALUE)
      .addRequired(USER)
      .build();

  public GuildMultiplierNode() {
    super("guildMultiplier", "multiplier");
    setPermission(Permissions.GUILD_ADMIN);
  }

  @Override
  protected void writeHelpInfo(TextWriter writer, CommandSource source) {
    writer.field("multiplier list", "Lists all active multipliers");

    writer.field(
        "multiplier add player=<player> value=<amount> length=<length>",
        "Adds an active multiplier with the given player as the source"
    );

    writer.field(
        "multiplier clear",
        "Clears all active multipliers"
    );

    writer.field(
        "multiplier remove <index>",
        "Removes the multiplier at the given index"
    );
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command
        .then(literal("list")
            .executes(c -> {
              var modifiers = GuildManager.get().getExpModifier();

              var writer = TextWriters.newWriter();
              writer.field("Calculated modifier", modifiers.getModifier());

              writer.field("Weekend Modifier", "{");
              var indented = writer.withIndent();
              indented.field("Value", GuildConfig.weekendModifier);
              indented.field("Active", modifiers.isWeekend());
              writer.line("}");

              if (!modifiers.getMultipliers().isEmpty()) {
                writer.field("Multipliers", "[");
                var prefixed = writer.withIndent();

                int i = 1;
                for (var d: modifiers.getMultipliers()) {
                  prefixed.line((i++) + ") ");

                  prefixed.formatted(
                      "Player={0, user}, modifier={1, number}, endsAt={2, date}",
                      d.getDonator(),
                      d.getModifier(),
                      d.getEndTime()
                  );
                }

                writer.line("]");
              }

              c.getSource().sendMessage(writer);
              return 0;
            })
        )

        .then(literal("clear")
            .executes(c -> {
              GuildManager.get()
                  .getExpModifier()
                  .clear();

              c.getSource().sendAdmin("Cleared all Guild Exp Multipliers");
              return 0;
            })
        )

        .then(literal("add")
            .then(argument("args", ARGS)
                .executes(c -> {
                  var parsedArgs = c.getArgument("args", ParsedArgs.class);

                  User user = parsedArgs.get(USER).get(c.getSource(), false);
                  float mod = parsedArgs.get(VALUE);
                  long timeMillis = parsedArgs.get(TIME_ARG);

                  Duration duration = Duration.ofMillis(timeMillis);

                  GuildManager.get()
                      .getExpModifier()
                      .addMultiplier(user.getUniqueId(), mod, duration);

                  c.getSource().sendAdmin(
                      Text.format(
                          "Activated GuildExp multiplier, "
                              + "value={0}, duration={1}, player={2, user}",
                          mod, duration,
                          user
                      )
                  );

                  return 0;
                })
            )
        )

        .then(literal("remove")
            .then(argument("index", IntegerArgumentType.integer(1))
                .executes(c -> {
                  var modifiers = GuildManager.get().getExpModifier();
                  int index = c.getArgument("index", Integer.class);
                  var list = modifiers.getMultipliers();

                  Commands.ensureIndexValid(index, list.size());

                  list.remove(index);
                  c.getSource().sendAdmin("Removed multiplier");
                  return 0;
                })
            )
        );
  }
}
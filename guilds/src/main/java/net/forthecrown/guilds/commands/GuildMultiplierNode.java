package net.forthecrown.guilds.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import java.time.Duration;
import java.util.UUID;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.UserParseResult;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.guilds.GuildPermissions;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.menu.GuildMenus;
import net.forthecrown.guilds.multiplier.MultiplierType;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.kyori.adventure.text.format.NamedTextColor;

public class GuildMultiplierNode extends GuildCommandNode {
  private static final ArgumentOption<Duration> TIME_ARG
      = Options.argument(ArgumentTypes.time(), "length");

  private static final ArgumentOption<Float> VALUE
      = Options.argument(FloatArgumentType.floatArg(), "value");

  private static final ArgumentOption<UserParseResult> USER
      = Options.argument(Arguments.USER, "player");

  private static final ArgumentOption<MultiplierType> TYPE_ARG
      = Options.argument(ArgumentTypes.enumType(MultiplierType.class), "type");

  private static final OptionsArgument ARGS = OptionsArgument.builder()
      .addRequired(TIME_ARG)
      .addRequired(VALUE)
      .addRequired(USER)
      .addRequired(TYPE_ARG)
      .build();

  public GuildMultiplierNode() {
    super("guildMultiplier", "multiplier");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("").addInfo("Opens the multiplier menu");
    factory = factory.withPermission(GuildPermissions.GUILD_ADMIN);

    factory.usage(
        "add player=<player> type=<guild | global> length=<time> value=<multiplier>",
        "Adds a multiplier"
    );

    factory.usage("manual", "Shows the current manual modifier");

    factory.usage("manual <value: number(0..)>")
        .addInfo("Sets the current manual modifier to a <value>");

    var prefixed = factory.withPrefix("<player> <type: global | guild>");
    prefixed.usage("")
        .addInfo("Lists all <type> multipliers a <player> has");

    prefixed.usage("remove <index>")
        .addInfo("Removes a <type> multiplier from <player>.")
        .addInfo("Use /g multiplier <player> <type> to list a <player>'s")
        .addInfo("multipliers");

    prefixed.usage("clear")
        .addInfo("Clears all <type> multipliers a <player> has");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command
        .executes(c -> {
          var user = getUserSender(c);

          GuildMenus.open(
              GuildMenus.MULTIPLIER_MENU,
              user,
              Guilds.getGuild(user)
          );
          return 0;
        })

        .then(argument("user", Arguments.USER)
            .requires(source -> source.hasPermission(GuildPermissions.GUILD_ADMIN))

            .then(argument("type", TYPE_ARG.getArgumentType())
                .requires(source -> source.hasPermission(GuildPermissions.GUILD_ADMIN))

                // Lists multipliers
                .executes(c -> {
                  var user = Arguments.getUser(c, "user");
                  var type = c.getArgument("type", MultiplierType.class);

                  var list = Guilds.getManager().getExpModifier()
                      .getMultipliers(user.getUniqueId(), type);

                  if (list.isEmpty()) {
                    throw Exceptions.NOTHING_TO_LIST;
                  }

                  var it = list.listIterator();

                  TextWriter writer = TextWriters.newWriter();
                  writer.formattedLine("{0, user}'s {1} multipliers: ",
                      user, type.getDisplayName()
                  );

                  while (it.hasNext()) {
                    var multiplier = it.next();
                    int index = it.nextIndex();

                    writer.formattedLine(
                        "{0, number}) {1, number}x {2, time, -short}",
                        index,
                        multiplier.getModifier(),
                        multiplier.getDuration()
                    );

                    if (multiplier.isActive()) {
                      writer.formatted(
                          " Remaining: {0, time, -short -biggest}",
                          multiplier.getRemainingMillis()
                      );
                    }
                  }

                  c.getSource().sendMessage(writer.asComponent());
                  return 0;
                })

                .then(literal("remove")
                    .requires(source -> source.hasPermission(GuildPermissions.GUILD_ADMIN))

                    .then(argument("index", IntegerArgumentType.integer(1))
                        .requires(source -> source.hasPermission(GuildPermissions.GUILD_ADMIN))

                        .executes(c -> {
                          var user = Arguments.getUser(c, "user");
                          var type
                              = c.getArgument("type", MultiplierType.class);

                          var modifiers = Guilds.getManager().getExpModifier();
                          var list = modifiers
                              .getMultipliers(user.getUniqueId(), type);

                          int index = c.getArgument("index", Integer.class);
                          Commands.ensureIndexValid(index, list.size());

                          var removed = list.remove(index - 1);
                          modifiers.remove(removed);

                          c.getSource().sendMessage("Removed multiplier");
                          return 0;
                        })
                    )
                )

                .then(literal("clear")
                    .requires(source -> source.hasPermission(GuildPermissions.GUILD_ADMIN))

                    .executes(c -> {
                      var user = Arguments.getUser(c, "user");
                      var type
                          = c.getArgument("type", MultiplierType.class);

                      var modifiers = Guilds.getManager().getExpModifier();
                      var list = modifiers
                          .getMultipliers(user.getUniqueId(), type);

                      if (list.isEmpty()) {
                        throw Exceptions.NOTHING_CHANGED;
                      }

                      list.forEach(modifiers::remove);

                      c.getSource().sendSuccess(
                          Text.format("Cleared all {0, user}'s {1} multipliers",
                              user, type.getDisplayName()
                          )
                      );
                      return 0;
                    })
                )
            )
        )

        .then(literal("manual")
            .requires(source -> source.hasPermission(GuildPermissions.GUILD_ADMIN))

            .executes(c -> {
              float val = Guilds.getManager().getExpModifier().getManual();

              c.getSource().sendMessage(
                  Text.format("Current manual modifier: &e{0, number}x&r.",
                      NamedTextColor.GRAY,
                      val
                  )
              );
              return 0;
            })

            .then(argument("value", FloatArgumentType.floatArg())
                .requires(source -> source.hasPermission(GuildPermissions.GUILD_ADMIN))

                .executes(c -> {
                  var multipliers = Guilds.getManager().getExpModifier();
                  float val = c.getArgument("value", Float.class);

                  multipliers.setManual(val);

                  c.getSource().sendSuccess(
                      Text.format(
                          "Set manual Guild Exp modifier to &e{0, number}x&r.",
                          NamedTextColor.GRAY,
                          val
                      )
                  );
                  return 0;
                })
            )
        )

        .then(literal("add")
            .requires(source -> source.hasPermission(GuildPermissions.GUILD_ADMIN))

            .then(argument("args", ARGS)
                .requires(source -> source.hasPermission(GuildPermissions.GUILD_ADMIN))

                .executes(c -> {
                  var parsedArgs = c.getArgument("args", ParsedOptions.class);

                  Duration duration = parsedArgs.getValue(TIME_ARG);
                  float scalar = parsedArgs.getValue(VALUE);
                  MultiplierType type = parsedArgs.getValue(TYPE_ARG);

                  UserParseResult result = parsedArgs.getValue(USER);
                  UUID id = result.get(c.getSource(), false).getUniqueId();

                  var modifiers = Guilds.getManager().getExpModifier();
                  modifiers.addMultiplier(id, scalar, duration, type);

                  c.getSource().sendSuccess(
                      Text.format(
                          "Added {0} multiplier to &e{1, user}&r: "
                              + "duration=&e{2, time}&r or {2, number}ms, "
                              + "scalar=&e{3, number}x&r.",
                          NamedTextColor.GRAY,
                          type.getDisplayName(), id, duration, scalar
                      )
                  );
                  return 0;
                })
            )
        );

  }
}
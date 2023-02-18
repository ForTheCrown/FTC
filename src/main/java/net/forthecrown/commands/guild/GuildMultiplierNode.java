package net.forthecrown.commands.guild;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import java.time.Duration;
import java.util.UUID;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.UserParseResult;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.guilds.multiplier.MultiplierType;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.menu.GuildMenus;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.format.NamedTextColor;

public class GuildMultiplierNode extends GuildCommandNode {
  private static final Argument<Long> TIME_ARG
      = Argument.builder("length", TimeArgument.time()).build();

  private static final Argument<Float> VALUE
      = Argument.builder("value", FloatArgumentType.floatArg()).build();

  private static final Argument<UserParseResult> USER
      = Argument.builder("player", Arguments.USER).build();

  private static final Argument<MultiplierType> TYPE_ARG
      = Argument.builder("type", EnumArgument.of(MultiplierType.class)).build();

  private static final ArgsArgument ARGS = ArgsArgument.builder()
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
    factory = factory.withPermission(Permissions.GUILD_ADMIN);

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
              user.getGuild()
          );
          return 0;
        })

        .then(argument("user", Arguments.USER)
            .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

            .then(argument("type", TYPE_ARG.getParser())
                .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

                // Lists multipliers
                .executes(c -> {
                  var user = Arguments.getUser(c, "user");
                  var type = c.getArgument("type", MultiplierType.class);

                  var list = GuildManager.get().getExpModifier()
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
                    .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

                    .then(argument("index", IntegerArgumentType.integer(1))
                        .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

                        .executes(c -> {
                          var user = Arguments.getUser(c, "user");
                          var type
                              = c.getArgument("type", MultiplierType.class);

                          var modifiers = GuildManager.get().getExpModifier();
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
                    .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

                    .executes(c -> {
                      var user = Arguments.getUser(c, "user");
                      var type
                          = c.getArgument("type", MultiplierType.class);

                      var modifiers = GuildManager.get().getExpModifier();
                      var list = modifiers
                          .getMultipliers(user.getUniqueId(), type);

                      if (list.isEmpty()) {
                        throw Exceptions.NOTHING_CHANGED;
                      }

                      list.forEach(modifiers::remove);

                      c.getSource().sendAdmin(
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
            .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

            .executes(c -> {
              float val = GuildManager.get().getExpModifier().getManual();

              c.getSource().sendMessage(
                  Text.format("Current manual modifier: &e{0, number}x&r.",
                      NamedTextColor.GRAY,
                      val
                  )
              );
              return 0;
            })

            .then(argument("value", FloatArgumentType.floatArg())
                .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

                .executes(c -> {
                  var multipliers = GuildManager.get().getExpModifier();
                  float val = c.getArgument("value", Float.class);

                  multipliers.setManual(val);

                  c.getSource().sendAdmin(
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
            .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

            .then(argument("args", ARGS)
                .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

                .executes(c -> {
                  var parsedArgs = c.getArgument("args", ParsedArgs.class);

                  long durationMillis = parsedArgs.get(TIME_ARG);
                  float scalar = parsedArgs.get(VALUE);
                  MultiplierType type = parsedArgs.get(TYPE_ARG);

                  UserParseResult result = parsedArgs.get(USER);
                  UUID id = result.get(c.getSource(), false).getUniqueId();

                  var modifiers = GuildManager.get().getExpModifier();
                  modifiers.addMultiplier(
                      id,
                      scalar,
                      Duration.ofMillis(durationMillis),
                      type
                  );

                  c.getSource().sendAdmin(
                      Text.format(
                          "Added {0} multiplier to &e{1, user}&r: "
                              + "duration=&e{2, time}&r or {2, number}ms, "
                              + "scalar=&e{3, number}x&r.",
                          NamedTextColor.GRAY,
                          type.getDisplayName(), id, durationMillis, scalar
                      )
                  );
                  return 0;
                })
            )
        );

  }
}
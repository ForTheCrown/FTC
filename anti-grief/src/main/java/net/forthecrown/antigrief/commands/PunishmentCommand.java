package net.forthecrown.antigrief.commands;

import com.google.common.base.Strings;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nullable;
import net.forthecrown.antigrief.GExceptions;
import net.forthecrown.antigrief.PunishEntry;
import net.forthecrown.antigrief.PunishType;
import net.forthecrown.antigrief.Punishment;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import org.bukkit.event.player.PlayerKickEvent;

public class PunishmentCommand extends FtcCommand {

  private final PunishType type;

  PunishmentCommand(String name, PunishType type, String... aliases) {
    super(name);

    this.type = type;
    setAliases(aliases);
    setPermission(type.getPermission());
    setDescription("Punishes a user with a " + type.presentableName());

    register();
  }

  static final ArgumentOption<String> REASON = Options.argument(new ReasonParser(), "reason");
  static final ArgumentOption<Duration> TIME = Options.argument(ArgumentTypes.time(), "length");

  static final OptionsArgument ARGS = OptionsArgument.builder()
      .addOptional(REASON)
      .addOptional(TIME)
      .build();

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user> [reason=<reason>] [length=<length: time>]")
        .addInfo("Punishes a user with a " + type.presentableName())
        .addInfo("If [length] is not set, the punishment will")
        .addInfo("not end automatically");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.USER)
            .executes(c -> punish(c, null, null))

            .then(argument("args", ARGS)
                .executes(c -> {
                  var args = c.getArgument("args", ParsedOptions.class);
                  var length = args.getValue(TIME);

                  return punish(c, args.getValue(REASON), length);
                })
            )
        );
  }

  int punish(CommandContext<CommandSource> c, @Nullable String reason, Duration length)
      throws CommandSyntaxException
  {
    User user = Arguments.getUser(c, "user");
    return punish(user, c.getSource(), reason, length);
  }

  int punish(User user, CommandSource source, @Nullable String reason, Duration length)
      throws CommandSyntaxException
  {
    if (!Punishments.canPunish(source, user)) {
      throw GExceptions.cannotPunish(user);
    }

    PunishEntry entry = Punishments.entry(user);

    if (entry.isPunished(type)) {
      throw GExceptions.alreadyPunished(user, type);
    }

    Punishments.handlePunish(user, source, reason, length, type, null);
    return 0;
  }

  public static void createCommands() {
    new PunishmentCommand("softmute", PunishType.SOFT_MUTE);
    new PunishmentCommand("mute", PunishType.MUTE);
    new CommandKick("kick", PunishType.KICK, "fkick", "kickplayer");
    new PunishmentCommand("ban", PunishType.BAN, "fban", "banish", "fbanish");
    new PunishmentCommand("ipban", PunishType.IP_BAN, "banip", "fbanip", "fipban");

    new CommandJails();
    new CommandJail();

    new PardonCommand("unsoftmute", PunishType.SOFT_MUTE, "pardonsoftmute");
    new PardonCommand("unmute", PunishType.MUTE, "pardonmute");
    new PardonCommand("unjail", PunishType.JAIL, "pardonjail");

    new PardonCommand("unban", PunishType.BAN, "pardonban");
    new PardonCommand("unbanip", PunishType.IP_BAN, "pardonip", "ippardon", "pardonipban");
  }

  public static class CommandKick extends PunishmentCommand {

    CommandKick(String name, PunishType type, String... aliases) {
      super(name, type, aliases);
    }

    @Override
    public void createCommand(GrenadierCommand command) {
      command.then(argument("user", Arguments.USER)
          .executes(c -> punish(c, null, null))

          .then(argument("reason", StringArgumentType.greedyString())
              .executes(c -> punish(c, c.getArgument("reason", String.class), null))
          )
      );
    }

    @Override
    int punish(User user, CommandSource source, @Nullable String reason, Duration length)
        throws CommandSyntaxException
    {
      if (!user.isOnline()) {
        throw GExceptions.CANNOT_KICK_OFFLINE;
      }

      user.getPlayer().kick(
          Strings.isNullOrEmpty(reason) ? null : Text.renderString(reason),
          PlayerKickEvent.Cause.KICK_COMMAND
      );

      // Record kick
      var entry = Punishments.get().getEntry(user.getUniqueId());
      entry.getPast().add(0, new Punishment(
          source.textName(),
          reason,
          null,
          PunishType.KICK,
          Instant.now(),
          null
      ));

      Punishments.announce(source, user, PunishType.KICK, null, reason);
      return 0;
    }
  }

}
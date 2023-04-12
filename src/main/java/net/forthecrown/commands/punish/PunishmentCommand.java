package net.forthecrown.commands.punish;

import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishment;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
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

  static final ArgumentOption<String> REASON
      = Options.argument(new ReasonParser())
      .addLabel("reason", "cause")
      .build();

  static final ArgumentOption<Duration> TIME
      = Options.argument(ArgumentTypes.time())
      .addLabel("time", "length")
      .build();

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
            .executes(c -> punish(c, null, INDEFINITE_EXPIRY))

            .then(argument("args", ARGS)
                .executes(c -> {
                  var args = c.getArgument("args", ParsedOptions.class);
                  long length = args.has(TIME)
                      ? args.getValue(TIME).toMillis()
                      : INDEFINITE_EXPIRY;

                  return punish(c, args.getValue(REASON), length);
                })
            )
        );
  }

  private int punish(CommandContext<CommandSource> c, @Nullable String reason, long length)
      throws CommandSyntaxException {
    User user = Arguments.getUser(c, "user");
    return punish(user, c.getSource(), reason, length);
  }

  int punish(User user, CommandSource source, @Nullable String reason, long length)
      throws CommandSyntaxException {
    if (!Punishments.canPunish(source, user)) {
      throw Exceptions.cannotPunish(user);
    }

    PunishEntry entry = Punishments.entry(user);

    if (entry.isPunished(type)) {
      throw Exceptions.alreadyPunished(user, type);
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

    new PardonCommand("unban", PunishType.BAN, "pardon", "pardonban");
    new PardonCommand("unbanip", PunishType.IP_BAN, "pardonip", "ippardon", "pardonipban");
  }

  public static class CommandKick extends PunishmentCommand {

    CommandKick(String name, PunishType type, String... aliases) {
      super(name, type, aliases);
    }

    @Override
    int punish(User user, CommandSource source, @Nullable String reason, long length)
        throws CommandSyntaxException {
      if (!user.isOnline()) {
        throw Exceptions.CANNOT_KICK_OFFLINE;
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
          System.currentTimeMillis(), INDEFINITE_EXPIRY
      ));

      Punishments.announce(source, user, PunishType.KICK, INDEFINITE_EXPIRY, reason);
      return 0;
    }
  }

  static class ReasonParser implements ArgumentType<String> {

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
      if (reader.peek() == '"' || reader.peek() == '\'') {
        return reader.readQuotedString();
      }

      var remaining = reader.getRemaining();
      reader.setCursor(reader.getTotalLength());

      return remaining;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                              SuggestionsBuilder builder
    ) {
      return Completions.suggest(builder, "\"\"", "''");
    }
  }
}
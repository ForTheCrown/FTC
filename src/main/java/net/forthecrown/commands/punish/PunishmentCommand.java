package net.forthecrown.commands.punish;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishment;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import org.bukkit.event.player.PlayerKickEvent;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;

public class PunishmentCommand extends FtcCommand {
    private final PunishType type;

    PunishmentCommand(String name, PunishType type, String... aliases) {
        super(name);

        this.type = type;
        setAliases(aliases);
        setPermission(type.getPermission());

        register();
    }

    static final Argument<String> REASON = Argument.builder("reason", new ReasonParser())
            .setAliases("cause")
            .build();

    static final Argument<Long> TIME = Argument.builder("time", TimeArgument.time())
            .setDefaultValue(INDEFINITE_EXPIRY)
            .setAliases("length")
            .build();

    static final ArgsArgument ARGS = ArgsArgument.builder()
            .addOptional(REASON)
            .addOptional(TIME)
            .build();

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", Arguments.USER)
                        .executes(c -> punish(c, null, INDEFINITE_EXPIRY))

                        .then(argument("args", ARGS)
                                .executes(c -> {
                                    var args = c.getArgument("args", ParsedArgs.class);
                                    return punish(c, args.get(REASON), args.get(TIME));
                                })
                        )
                );
    }

    private int punish(CommandContext<CommandSource> c, @Nullable String reason, long length) throws CommandSyntaxException {
        User user = Arguments.getUser(c, "user");
        return punish(user, c.getSource(), reason, length);
    }

    int punish(User user, CommandSource source, @Nullable String reason, long length) throws CommandSyntaxException {
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
        new PunishmentCommand( "softmute",  PunishType.SOFT_MUTE                                  );
        new PunishmentCommand( "mute",      PunishType.MUTE                                       );
        new CommandKick(       "kick",      PunishType.KICK,     "fkick", "kickplayer"            );
        new PunishmentCommand( "ban",       PunishType.BAN,      "fban",  "banish",      "fbanish");
        new PunishmentCommand( "ipban",     PunishType.IP_BAN,   "banip", "fbanip",      "fipban" );

        new CommandJails();
        new CommandJail();

        new PardonCommand("unsoftmute", PunishType.SOFT_MUTE,   "pardonsoftmute");
        new PardonCommand("unmute",     PunishType.MUTE,        "pardonmute");
        new PardonCommand("unjail",     PunishType.JAIL,        "pardonjail");

        new PardonCommand("unban",      PunishType.BAN,         "pardon", "pardonban");
        new PardonCommand("unbanip",    PunishType.IP_BAN,      "pardonip", "ippardon", "pardonipban");
    }

    public static class CommandKick extends PunishmentCommand {
        CommandKick(String name, PunishType type, String... aliases) {
            super(name, type, aliases);
        }

        @Override
        int punish(User user, CommandSource source, @Nullable String reason, long length) throws CommandSyntaxException {
            if (!user.isOnline()) {
                throw Exceptions.CANNOT_KICK_OFFLINE;
            }

            user.getPlayer().kick(
                    Util.isNullOrBlank(reason) ? null : Text.renderString(reason),
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
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return CompletionProvider.suggestMatching(builder, "\"\"", "''");
        }
    }
}
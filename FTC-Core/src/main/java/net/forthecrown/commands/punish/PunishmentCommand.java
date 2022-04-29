package net.forthecrown.commands.punish;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerKickEvent;

import javax.annotation.Nullable;

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

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.user())
                        .executes(c -> punish(c, null, INDEFINITE_EXPIRY))

                        .then(argument("time", TimeArgument.time())
                                .executes(c -> punish(c, null, TimeArgument.getMillis(c, "time")))

                                .then(argument("reason", StringArgumentType.greedyString())
                                        .executes(c -> punish(
                                                c,
                                                c.getArgument("reason", String.class),
                                                TimeArgument.getMillis(c, "time")
                                        ))
                                )
                        )
                );
    }

    private int punish(CommandContext<CommandSource> c, @Nullable String reason, long length) throws CommandSyntaxException {
        CrownUser user = UserArgument.getUser(c, "user");
        return punish(user, c.getSource(), reason, length);
    }

    int punish(CrownUser user, CommandSource source, @Nullable String reason, long length) throws RoyalCommandException {
        if (!Punishments.canPunish(source, user)) {
            throw FtcExceptionProvider.create("Cannot punish " + user.getName());
        }

        PunishEntry entry = Punishments.entry(user);

        if (entry.isPunished(type)) {
            throw FtcExceptionProvider.create(user.getNickOrName() + " has already been " + type.nameEndingED());
        }

        Punishments.handlePunish(user, source, reason, length, type, null);
        return 0;
    }

    public static void init() {
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

    static class CommandKick extends PunishmentCommand {
        CommandKick(String name, PunishType type, String... aliases) {
            super(name, type, aliases);
        }

        @Override
        int punish(CrownUser user, CommandSource source, @Nullable String reason, long length) throws RoyalCommandException {
            if (!user.isOnline()) {
                throw FtcExceptionProvider.create("Cannot kick offline user");
            }

            user.getPlayer().kick(
                    FtcUtils.isNullOrBlank(reason) ? null : Component.text(reason),
                    PlayerKickEvent.Cause.KICK_COMMAND
            );

            Punishments.announce(source, user, PunishType.KICK, INDEFINITE_EXPIRY, reason);
            return 0;
        }
    }
}
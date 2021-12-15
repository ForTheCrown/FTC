package net.forthecrown.commands.punishments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.record.PunishmentType;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandMute extends FtcCommand implements TempPunisher {
    public CommandMute(){
        super("mute", Crown.inst());

        setPermission(Permissions.POLICE);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.user())
                        .executes(c -> punish(
                                UserArgument.getUser(c, "user"),
                                c.getSource(),
                                -1,
                                null
                        ))

                        .then(argument("time", TimeArgument.time())
                                .executes(c -> punish(
                                        UserArgument.getUser(c, "user"),
                                        c.getSource(),
                                        TimeArgument.getMillis(c, "time"),
                                        null
                                ))
                        )
                );
    }

    @Override
    public int punish(CrownUser user, CommandSource source, long length, String reason) throws CommandSyntaxException {
        PunishmentManager manager = Crown.getPunishmentManager();

        if(manager.checkMute(user.getPlayer()) == MuteStatus.HARD){
            manager.pardon(user.getUniqueId(), PunishmentType.MUTE);
            length = 0;
        } else {
            long totalTime = lengthTranslate(length);

            manager.punish(user.getUniqueId(), PunishmentType.MUTE, source, reason, totalTime);
        }

        source.sendAdmin(
                Component.text((length == 0 ? "Unm" : "M") + "uting ")
                        .color(NamedTextColor.YELLOW)
                        .append(user.displayName().color(NamedTextColor.GOLD))
                        .append(Component.text(length > 1 ? " for " + FtcFormatter.convertMillisIntoTime(length) : ""))
        );
        return 0;
    }
}

package net.forthecrown.emperor.commands.punishments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.admin.MuteStatus;
import net.forthecrown.emperor.admin.PunishmentManager;
import net.forthecrown.emperor.admin.record.PunishmentType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.TimeArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandMute extends CrownCommandBuilder implements TempPunisher {
    public CommandMute(){
        super("mute", CrownCore.inst());

        setPermission(Permissions.POLICE);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.user())
                        .executes(c -> punish(
                                UserType.getUser(c, "user"),
                                c.getSource(),
                                -1,
                                null
                        ))

                        .then(argument("time", TimeArgument.time())
                                .executes(c -> punish(
                                        UserType.getUser(c, "user"),
                                        c.getSource(),
                                        TimeArgument.getMillis(c, "time"),
                                        null
                                ))
                        )
                );
    }

    @Override
    public int punish(CrownUser user, CommandSource source, long length, String reason) throws CommandSyntaxException {
        if(user.hasPermission(Permissions.MUTE_BYPASS)) throw FtcExceptionProvider.cannotMute(user);

        PunishmentManager manager = CrownCore.getPunishmentManager();

        if(manager.checkMute(user.getPlayer()) == MuteStatus.HARD){
            manager.pardon(user.getUniqueId(), PunishmentType.MUTE);
            length = 0;
        } else {
            long totalTime = lengthTranslate(length);

            manager.punish(user.getUniqueId(), PunishmentType.MUTE, source, null, totalTime);
        }

        source.sendAdmin(
                Component.text((length == 0 ? "Unm" : "M") + "uting ")
                        .color(NamedTextColor.YELLOW)
                        .append(user.displayName().color(NamedTextColor.GOLD))
                        .append(Component.text(length > 1 ? " for " + ChatFormatter.convertMillisIntoTime(length) : ""))
        );
        return 0;
    }
}

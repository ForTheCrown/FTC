package net.forthecrown.commands.punishments;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.PunishmentEntry;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.admin.record.PunishmentRecord;
import net.forthecrown.core.admin.record.PunishmentType;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.TimeArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Date;

public class CommandTempBan extends FtcCommand implements TempPunisher {
    public CommandTempBan(){
        super("tempban", Crown.inst());

        setPermission(Permissions.POLICE);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.user())
                        .then(argument("time", TimeArgument.time())
                                .executes(c -> punish(
                                        UserArgument.getUser(c, "user"),
                                        c.getSource(),
                                        c.getArgument("time", Long.class),
                                        null
                                ))

                                .then(argument("reason", StringArgumentType.greedyString())
                                        .executes(c -> punish(
                                                UserArgument.getUser(c, "user"),
                                                c.getSource(),
                                                c.getArgument("time", Long.class),
                                                c.getArgument("reason", String.class)
                                        ))
                                )
                        )
                );
    }

    @Override
    public int punish(CrownUser user, CommandSource source, long length, String reason) throws CommandSyntaxException {
        if(user.hasPermission(Permissions.BAN_BYPASS) && !source.is(ConsoleCommandSender.class)) throw FtcExceptionProvider.cannotBan(user);

        PunishmentManager manager = Crown.getPunishmentManager();
        BanList list = Bukkit.getBanList(BanList.Type.NAME);
        long until = lengthTranslate(length);

        PunishmentEntry entry = manager.getEntry(user.getUniqueId());
        if(entry != null && entry.checkPunished(PunishmentType.BAN)) throw FtcExceptionProvider.create("User has already been banned");

        PunishmentRecord record = manager.punish(user.getUniqueId(), PunishmentType.BAN, source, reason, until);
        if(user.isOnline()) user.getPlayer().kick(FtcFormatter.banMessage(record));

        list.addBan(user.getName(), reason, new Date(until), source.textName());

        StaffChat.sendCommand(
                source,
                Component.text("Banned ")
                        .color(NamedTextColor.YELLOW)
                        .append(user.displayName().color(NamedTextColor.GOLD))
                        .append(Component.text(" for "))
                        .append(FtcFormatter.millisIntoTime(length).color(NamedTextColor.GOLD))
        );
        return 0;
    }
}

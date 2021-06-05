package net.forthecrown.emperor.commands.punishments;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.admin.PunishmentEntry;
import net.forthecrown.emperor.admin.PunishmentManager;
import net.forthecrown.emperor.admin.StaffChat;
import net.forthecrown.emperor.admin.record.PunishmentRecord;
import net.forthecrown.emperor.admin.record.PunishmentType;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.utils.ChatFormatter;
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
        super("tempban", CrownCore.inst());

        setPermission(Permissions.POLICE);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.user())
                        .then(argument("time", TimeArgument.time())
                                .executes(c -> punish(
                                        UserType.getUser(c, "user"),
                                        c.getSource(),
                                        c.getArgument("time", Long.class),
                                        null
                                ))

                                .then(argument("reason", StringArgumentType.greedyString())
                                        .executes(c -> punish(
                                                UserType.getUser(c, "user"),
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

        PunishmentManager manager = CrownCore.getPunishmentManager();
        BanList list = Bukkit.getBanList(BanList.Type.NAME);

        PunishmentEntry entry = manager.getEntry(user.getUniqueId());
        if(entry != null && entry.checkPunished(PunishmentType.BAN)) throw FtcExceptionProvider.create("User has already been banned");

        PunishmentRecord record = manager.punish(user.getUniqueId(), PunishmentType.BAN, source, reason, length);
        if(user.isOnline()) user.getPlayer().kick(ChatFormatter.banMessage(record));

        list.addBan(user.getName(), reason, new Date(length + System.currentTimeMillis()), source.textName());

        StaffChat.sendCommand(
                source,
                Component.text("Banned ")
                        .color(NamedTextColor.YELLOW)
                        .append(user.displayName().color(NamedTextColor.GOLD))
                        .append(Component.text(" for "))
                        .append(ChatFormatter.millisIntoTime(length).color(NamedTextColor.GOLD))
        );
        return 0;
    }
}

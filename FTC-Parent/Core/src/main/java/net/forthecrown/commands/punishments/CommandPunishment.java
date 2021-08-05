package net.forthecrown.commands.punishments;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.PunishmentEntry;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.admin.record.PunishmentRecord;
import net.forthecrown.core.admin.record.PunishmentType;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.Permission;

public class CommandPunishment extends FtcCommand {

    private final Punisher punisher;
    public CommandPunishment(String name, Permission perm, Punisher punisher, String... aliases){
        super(name, ForTheCrown.inst());

        this.punisher = punisher;
        this.aliases = aliases;
        this.permission = perm;

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.user())
                        .executes(c -> punisher.punish(
                                UserArgument.getUser(c, "user"),
                                c.getSource(),
                                null
                        ))

                        .then(argument("reason", StringArgumentType.greedyString())
                                .executes(c -> punisher.punish(
                                        UserArgument.getUser(c, "user"),
                                        c.getSource(),
                                        c.getArgument("reason", String.class)
                                ))
                        )
                );
    }

    public static void init(){
        new CommandPunishment(
                "ban_ftc",
                Permissions.POLICE,
                (user, source, reason) -> {
                    if(user.hasPermission(Permissions.BAN_BYPASS) && !source.is(ConsoleCommandSender.class)) throw FtcExceptionProvider.cannotBan(user);

                    PunishmentManager manager = ForTheCrown.getPunishmentManager();
                    PunishmentEntry entry = manager.getEntry(user.getUniqueId());
                    if(entry != null && entry.checkPunished(PunishmentType.BAN)) throw FtcExceptionProvider.create("User has already been banned");

                    PunishmentRecord record = manager.punish(user.getUniqueId(), PunishmentType.BAN, source, reason, -1);

                    if(user.isOnline()) user.getPlayer().kick(FtcFormatter.banMessage(record));

                    BanList list = Bukkit.getBanList(BanList.Type.NAME);
                    list.addBan(user.getName(), reason, null, source.textName());

                    StaffChat.sendCommand(
                            source,
                            Component.text("Banned ")
                                    .color(NamedTextColor.YELLOW)
                                    .append(user.nickDisplayName().color(NamedTextColor.GOLD))
                                    .append(reason != null ? Component.text(", reason: " + reason).color(NamedTextColor.GOLD) : Component.empty())
                    );
                    return 0;
                }, "ban", "banish"
        );

        new CommandPunishment(
                "kick_ftc",
                Permissions.POLICE,
                (user, source, reason) -> {
                    if(user.hasPermission(Permissions.KICK_BYPASS) && !source.is(ConsoleCommandSender.class)) throw FtcExceptionProvider.cannotKick(user);

                    user.getPlayer().kick(reason == null ? null : ChatUtils.convertString(reason, true));

                    StaffChat.sendCommand(
                            source,
                            Component.text("Kicked ")
                                    .color(NamedTextColor.YELLOW)
                                    .append(user.displayName().color(NamedTextColor.GOLD))
                                    .append(reason != null ? Component.text(", reason: " + reason).color(NamedTextColor.GOLD) : Component.empty())
                    );
                    return 0;
                }, "kick"
        );
    }
}

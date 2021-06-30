package net.forthecrown.commands.punishments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.CoreCommands;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.arguments.JailType;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.events.dynamic.JailListener;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.admin.jails.JailManager;
import net.forthecrown.core.admin.record.PunishmentType;
import net.forthecrown.user.CrownUser;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.TimeArgument;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class CommandJail extends FtcCommand implements GenericPunisher {
    public CommandJail(){
        super("jail", CrownCore.inst());

        setPermission(Permissions.HELPER);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.user())
                        .then(argument("jail", JailType.jail())
                                .executes(c -> punish(
                                        UserType.getUser(c, "user"),
                                        c.getSource(),
                                        -1,
                                        c.getArgument("jail", Key.class)
                                ))

                                .then(argument("time", TimeArgument.time())
                                        .executes(c -> punish(
                                                UserType.getUser(c, "user"),
                                                c.getSource(),
                                                TimeArgument.getMillis(c, "time"),
                                                c.getArgument("jail", Key.class)
                                        ))
                                )
                        )
                );
    }

    public int punish(CrownUser user, CommandSource source, long length, Key jail) throws CommandSyntaxException {
        if(user.hasPermission(Permissions.JAIL_BYPASS) && !source.is(ConsoleCommandSender.class)) throw FtcExceptionProvider.cannotJail(user);

        PunishmentManager manager = CrownCore.getPunishmentManager();
        JailManager jails = CrownCore.getJailManager();

        if(manager.checkJailed(user.getPlayer())) throw FtcExceptionProvider.create("Player is already jailed");

        long punishTime = lengthTranslate(length);
        manager.punish(user.getUniqueId(), PunishmentType.JAIL, source, null, punishTime, jail.asString());

        if(user.isOnline()){
            CoreCommands.resendCommandPackets(user.getPlayer());
            Bukkit.getPluginManager().registerEvents(new JailListener(user.getPlayer(), jails.get(jail)), CrownCore.inst());
        }

        StaffChat.sendCommand(
                source,
                Component.text("Jailed ")
                        .color(NamedTextColor.YELLOW)
                        .append(user.displayName().color(NamedTextColor.GOLD))
                        .append(Component.text(punishTime == -1 ? "" : " for " + ChatFormatter.convertMillisIntoTime(length)))
        );
        return 0;
    }
}

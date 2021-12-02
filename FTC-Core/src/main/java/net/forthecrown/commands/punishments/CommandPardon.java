package net.forthecrown.commands.punishments;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.PunishmentEntry;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.admin.jails.JailManager;
import net.forthecrown.core.admin.record.PunishmentType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;

import java.util.UUID;

public class CommandPardon extends FtcCommand {
    public CommandPardon(){
        super("pardon_ftc", Crown.inst());

        setAliases("pardon");
        setPermission(Permissions.HELPER);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("ban")
                        .requires(s -> s.hasPermission(Permissions.POLICE))

                        .then(argument("name", StringArgumentType.word())
                                .suggests((c, b) -> CompletionProvider.suggestMatching(b, ListUtils.convert(Bukkit.getBanList(BanList.Type.NAME).getBanEntries(), BanEntry::getTarget)))

                                .executes(c -> {
                                    BanList list = Bukkit.getBanList(BanList.Type.NAME);
                                    String name = c.getArgument("name", String.class);

                                    if(list.isBanned(name)) list.pardon(name);

                                    UUID id = FtcUtils.uuidFromName(name);
                                    if(id != null){
                                        PunishmentEntry entry = Crown.getPunishmentManager().getEntry(id);
                                        if(entry != null) entry.pardon(PunishmentType.BAN);
                                    }

                                    StaffChat.sendCommand(
                                            c.getSource(),
                                            Component.text("Unbanned ")
                                                    .color(NamedTextColor.GRAY)
                                                    .append(Component.text(name).color(NamedTextColor.YELLOW))
                                    );
                                    return 0;
                                })
                        )
                )

                .then(argument("user", UserArgument.user())
                        .then(literal("softmute")
                                .executes(c -> {
                                    CrownUser user = user(c);
                                    PunishmentManager manager = Crown.getPunishmentManager();

                                    if(manager.isSoftmuted(user.getUniqueId())) throw FtcExceptionProvider.create(user.getName() + " is not softmuted");

                                    manager.pardon(user.getUniqueId(), PunishmentType.SOFT_MUTE);

                                    StaffChat.sendCommand(
                                            c.getSource(),
                                            Component.text("Unmuting ")
                                                    .color(NamedTextColor.YELLOW)
                                                    .append(user.displayName().color(NamedTextColor.GOLD))
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("jail")
                                .executes(c -> {
                                    CrownUser user = user(c);
                                    PunishmentManager manager = Crown.getPunishmentManager();
                                    JailManager jails = manager.getJailManager();

                                    if(!manager.checkJailed(user.getPlayer())) throw FtcExceptionProvider.create(user.getName() + " is not jailed");

                                    manager.pardon(user.getUniqueId(), PunishmentType.JAIL);
                                    if(user.isOnline()) jails.getListener(user.getPlayer()).release();

                                    StaffChat.sendCommand(
                                            c.getSource(),
                                            Component.text("Unjailing ")
                                                    .append(user.nickDisplayName())
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("mute")
                                .requires(s -> s.hasPermission(Permissions.POLICE))

                                .executes(c -> {
                                    CrownUser user = user(c);
                                    PunishmentManager manager = Crown.getPunishmentManager();

                                    if(manager.isMuted(user.getUniqueId())) throw FtcExceptionProvider.create(user.getName() + " is not muted");

                                    manager.pardon(user.getUniqueId(), PunishmentType.MUTE);
                                    StaffChat.sendCommand(
                                            c.getSource(),
                                            Component.text("Unmuting ")
                                                    .color(NamedTextColor.YELLOW)
                                                    .append(user.displayName().color(NamedTextColor.GOLD))
                                    );
                                    return 0;
                                })
                        )
                );
    }

    private CrownUser user(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return UserArgument.getUser(c, "user");
    }
}

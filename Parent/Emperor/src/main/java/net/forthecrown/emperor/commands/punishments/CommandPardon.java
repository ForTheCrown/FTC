package net.forthecrown.emperor.commands.punishments;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.admin.PunishmentEntry;
import net.forthecrown.emperor.admin.PunishmentManager;
import net.forthecrown.emperor.admin.StaffChat;
import net.forthecrown.emperor.admin.record.PunishmentType;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.emperor.utils.ListUtils;
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
        super("pardon_ftc", CrownCore.inst());

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

                                    UUID id = CrownUtils.uuidFromName(name);
                                    if(id != null){
                                        PunishmentEntry entry = CrownCore.getPunishmentManager().getEntry(id);
                                        if(entry != null) entry.pardon(PunishmentType.BAN);
                                    }

                                    c.getSource().sendAdmin(
                                            Component.text("Unbanned ")
                                                    .color(NamedTextColor.GRAY)
                                                    .append(Component.text(name).color(NamedTextColor.YELLOW))
                                    );
                                    return 0;
                                })
                        )
                )

                .then(argument("user", UserType.user())
                        .then(literal("softmute")
                                .executes(c -> {
                                    CrownUser user = user(c);
                                    PunishmentManager manager = CrownCore.getPunishmentManager();

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
                                    PunishmentManager manager = CrownCore.getPunishmentManager();

                                    if(!manager.checkJailed(user.getPlayer())) throw FtcExceptionProvider.create(user.getName() + " is not jailed");

                                    manager.pardon(user.getUniqueId(), PunishmentType.JAIL);

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
                                    PunishmentManager manager = CrownCore.getPunishmentManager();

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
        return UserType.getUser(c, "user");
    }
}

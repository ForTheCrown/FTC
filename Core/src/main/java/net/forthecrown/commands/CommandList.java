package net.forthecrown.commands;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import java.util.Collection;

public class CommandList extends FtcCommand {
    public CommandList(){
        super("flist", Crown.inst());

        setAliases("list", "elist", "playerlist");
        setPermission(Permissions.HELPER);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Collection<CrownUser> users = UserManager.getOnlineUsers();
                    TextComponent.Builder builder = Component.text()
                            .color(NamedTextColor.YELLOW)

                            .append(Component.text("There are "))
                            .append(Component.text(users.size()).color(NamedTextColor.GOLD))
                            .append(Component.text(" out of "))
                            .append(Component.text(Bukkit.getMaxPlayers()).color(NamedTextColor.GOLD))
                            .append(Component.text(" Players online."))

                            .append(Component.newline())
                            .append(Component.text("Players: "));

                    boolean firstIter = true;
                    for (CrownUser u: users){
                        builder
                                .append(firstIter ? Component.empty() : Component.text(", "))
                                .append(u.nickDisplayName().color(NamedTextColor.WHITE))
                                .append(u.isVanished() ? Component.text(" [Hidden]").color(NamedTextColor.GRAY) : Component.empty())
                                .append(u.isAfk() ? Component.text(" [AFK]").color(NamedTextColor.GRAY) : Component.empty());

                        firstIter = false;
                    }

                    c.getSource().sendMessage(builder.build());
                    return 0;
                });
    }
}

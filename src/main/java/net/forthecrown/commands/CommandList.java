package net.forthecrown.commands;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.kyori.adventure.text.Component;

import java.util.Set;

public class CommandList extends FtcCommand {
    public CommandList(){
        super("flist");

        setAliases("list", "elist", "playerlist");
        setPermission(Permissions.CMD_LIST);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Set<User> users = Users.getOnline();

                    // If we should hide vanished
                    if (!c.getSource().hasPermission(Permissions.VANISH_SEE)) {
                        users.removeIf(user -> user.get(Properties.VANISHED));
                    }

                    // lol
                    if (users.isEmpty()) {
                        throw Exceptions.EMPTY_SERVER;
                    }

                    c.getSource().sendMessage(
                            Messages.listHeader(users.size())
                                    .append(Component.newline())
                                    .append(Messages.listPlayers(users))
                    );
                    return 0;
                });
    }
}
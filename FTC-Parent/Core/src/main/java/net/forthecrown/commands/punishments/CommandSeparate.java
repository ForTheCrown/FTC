package net.forthecrown.commands.punishments;

import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.kyori.adventure.text.Component;

public class CommandSeparate extends FtcCommand {

    public CommandSeparate() {
        super("seperate");

        setPermission(Permissions.POLICE);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Forces two people to ignore each other
     *
     * Valid usages of command:
     * /seperate <first user> <second user>
     *
     * Permissions used:
     * ftc.police
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("first", UserArgument.user())
                        .then(argument("second", UserArgument.user())
                                .executes(c -> {
                                    CrownUser first = UserArgument.getUser(c, "first");
                                    CrownUser second = UserArgument.getUser(c, "second");

                                    UserInteractions firstInter = first.getInteractions();
                                    UserInteractions secondInter = second.getInteractions();

                                    if(firstInter.isSeparatedPlayer(second.getUniqueId()) && secondInter.isSeparatedPlayer(first.getUniqueId())){
                                        firstInter.removeSeparated(second.getUniqueId());
                                        secondInter.removeSeparated(first.getUniqueId());

                                        StaffChat.sendCommand(
                                                c.getSource(),
                                                Component.text("Unseparating ")
                                                        .append(first.displayName())
                                                        .append(Component.text(" and "))
                                                        .append(second.displayName())
                                        );
                                    } else {
                                        firstInter.addSeparated(second.getUniqueId());
                                        secondInter.addSeparated(first.getUniqueId());

                                        StaffChat.sendCommand(
                                                c.getSource(),
                                                Component.text("Forcing ")
                                                        .append(first.displayName())
                                                        .append(Component.text(" and "))
                                                        .append(second.displayName())
                                                        .append(Component.text(" to ignore each other."))
                                        );
                                    }

                                    return 0;
                                })
                        )
                );
    }
}
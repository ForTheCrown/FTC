package net.forthecrown.commands.punish;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.data.UserInteractions;

public class CommandSeparate extends FtcCommand {

    public CommandSeparate() {
        super("seperate");

        setPermission(Permissions.PUNISH_SEPARATE);
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
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("first", Arguments.USER)
                        .then(argument("second", Arguments.USER)
                                .executes(c -> {
                                    User first = Arguments.getUser(c, "first");
                                    User second = Arguments.getUser(c, "second");

                                    UserInteractions firstInter = first.getInteractions();
                                    UserInteractions secondInter = second.getInteractions();

                                    // Both users' lists separated lists have to contain
                                    // the other player, just to make sure
                                    if (firstInter.isSeparatedPlayer(second.getUniqueId())
                                            && secondInter.isSeparatedPlayer(first.getUniqueId())
                                    ) {
                                        firstInter.removeSeparated(second.getUniqueId());
                                        secondInter.removeSeparated(first.getUniqueId());

                                        StaffChat.sendCommand(
                                                c.getSource(),
                                                Messages.unseparating(first ,second)
                                        );
                                    } else {
                                        firstInter.addSeparated(second.getUniqueId());
                                        secondInter.addSeparated(first.getUniqueId());

                                        StaffChat.sendCommand(
                                                c.getSource(),
                                                Messages.separating(first ,second)
                                        );
                                    }

                                    return 0;
                                })
                        )
                );
    }
}
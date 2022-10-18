package net.forthecrown.commands.tpa;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.TeleportRequest;
import net.forthecrown.user.User;

public class CommandTpaAccept extends FtcCommand {
    public CommandTpaAccept(){
        super("tpaccept");

        setPermission(Permissions.TPA);
        setDescription("Accepts a tpa request");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", Arguments.ONLINE_USER)
                        .executes(c -> {
                            User user = getUserSender(c);
                            User target = Arguments.getUser(c, "user");

                            TeleportRequest r = user.getInteractions().getIncoming(target);

                            if (r == null) {
                                throw Exceptions.noIncoming(target);
                            }

                            r.accept();
                            return 0;
                        })
                )

                .executes(c -> {
                    User user = getUserSender(c);
                    TeleportRequest r = user.getInteractions().latestIncoming();

                    if (r == null) {
                        throw Exceptions.NO_TP_REQUESTS;
                    }

                    r.accept();
                    return 0;
                });
    }
}
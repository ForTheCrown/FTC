package net.forthecrown.commands.tpa;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.TeleportRequest;
import net.forthecrown.user.User;

public class CommandTpaCancel extends FtcCommand {
    public CommandTpaCancel(){
        super("tpacancel");

        setPermission(Permissions.TPA);
        setDescription("Cancels a tpa request");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", Arguments.ONLINE_USER)
                        .executes(c -> {
                            User user = getUserSender(c);
                            User target = Arguments.getUser(c, "user");

                            TeleportRequest r = user.getInteractions().getOutgoing(target);
                            if(r == null) {
                                throw Exceptions.noOutgoing(target);
                            }

                            r.cancel();
                            return 0;
                        })
                )

                .executes(c -> {
                    User user = getUserSender(c);
                    TeleportRequest r = user.getInteractions().latestOutgoing();

                    if(r == null) {
                        throw Exceptions.NO_TP_REQUESTS;
                    }

                    r.cancel();
                    return 0;
                });
    }
}
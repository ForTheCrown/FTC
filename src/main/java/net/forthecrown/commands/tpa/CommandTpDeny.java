package net.forthecrown.commands.tpa;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.TeleportRequest;
import net.forthecrown.user.User;

public class CommandTpDeny extends FtcCommand {
    public CommandTpDeny(){
        super("tpdeny");

        setPermission(Permissions.TPA);
        setDescription("Denies a tpa request");
        setAliases("tpadeny");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", Arguments.ONLINE_USER)
                        .executes(c -> {
                            User user = getUserSender(c);
                            User target = Arguments.getUser(c, "user");
                            TeleportRequest request = user.getInteractions().getIncoming(target);

                            if (request == null) {
                                throw Exceptions.noOutgoing(target);
                            }

                            request.deny();
                            return 0;
                        })
                )

                .executes(c -> {
                    User user = getUserSender(c);
                    TeleportRequest first = user.getInteractions().latestIncoming();

                    if (first == null) {
                        throw Exceptions.NO_TP_REQUESTS;
                    }

                    first.deny();
                    return 0;
                });
    }
}
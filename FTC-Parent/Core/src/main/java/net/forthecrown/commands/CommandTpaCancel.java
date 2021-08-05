package net.forthecrown.commands;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.TeleportRequest;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandTpaCancel extends FtcCommand {
    public CommandTpaCancel(){
        super("tpacancel", ForTheCrown.inst());

        setPermission(Permissions.TPA);
        setDescription("Cancels a tpa request");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.onlineUser())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserArgument.getUser(c, "user");

                            TeleportRequest r = user.getInteractions().getOutgoing(target);
                            if(r == null) throw FtcExceptionProvider.noOutgoingTP(target);

                            r.cancel();
                            return 0;
                        })
                )

                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    TeleportRequest r = user.getInteractions().firstOutgoing();
                    if(r == null) throw FtcExceptionProvider.noTpRequest();

                    r.cancel();
                    return 0;
                });
    }
}

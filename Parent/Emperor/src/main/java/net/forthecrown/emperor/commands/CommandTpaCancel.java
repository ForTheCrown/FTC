package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.data.TeleportRequest;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandTpaCancel extends FtcCommand {
    public CommandTpaCancel(){
        super("tpacancel", CrownCore.inst());

        setPermission(Permissions.TPA);
        setDescription("Cancels a tpa request");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.onlineUser())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserType.getUser(c, "user");

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

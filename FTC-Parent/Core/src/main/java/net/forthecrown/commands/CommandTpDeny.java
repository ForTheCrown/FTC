package net.forthecrown.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.data.TeleportRequest;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandTpDeny extends FtcCommand {
    public CommandTpDeny(){
        super("tpdeny", CrownCore.inst());

        setPermission(Permissions.TPA);
        setDescription("Denies a tpa request");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.onlineUser())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserType.getUser(c, "user");
                            TeleportRequest request = user.getInteractions().getIncoming(target);

                            if(request == null) throw FtcExceptionProvider.noOutgoingTP(target);

                            request.onDeny(true);
                            return 0;
                        })
                )

                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    TeleportRequest first = user.getInteractions().firstIncoming();

                    if(first == null) throw FtcExceptionProvider.noTpRequest();

                    first.onDeny(true);
                    return 0;
                });
    }
}

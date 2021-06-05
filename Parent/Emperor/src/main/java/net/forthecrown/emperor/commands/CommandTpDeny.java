package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.data.TeleportRequest;
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

package net.forthecrown.commands;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.TeleportRequest;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandTpaAccept extends FtcCommand {
    public CommandTpaAccept(){
        super("tpaccept", ForTheCrown.inst());

        setPermission(Permissions.TPA);
        setDescription("Accepts a tpa request");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.onlineUser())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserType.getUser(c, "user");

                            TeleportRequest r = user.getInteractions().getIncoming(target);
                            if(r == null) throw FtcExceptionProvider.noIncomingTP(target);

                            r.onAccept();
                            return 0;
                        })
                )

                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    TeleportRequest r = user.getInteractions().firstIncoming();
                    if(r == null) throw FtcExceptionProvider.noTpRequest();

                    r.onAccept();
                    return 0;
                });
    }
}

package net.forthecrown.commands.tpa;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;

public class CommandTpCancel extends FtcCommand {
    public CommandTpCancel(){
        super("tpcancel");

        setPermission(Permissions.TPA);
        setDescription("Cancels a teleport");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            User user = getUserSender(c);

            if(user.isTeleporting()) {
                throw Exceptions.NOT_CURRENTLY_TELEPORTING;
            }

            user.getLastTeleport().interrupt();
            return 0;
        });
    }
}
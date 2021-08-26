package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandCrownTop extends FtcCommand {

    public CommandCrownTop(){
        super("crowntop", Crown.inst());

        setPermission(Permissions.DEFAULT);
        setDescription("Shows the players with the most crown score");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            if(!ComVars.isEventActive()) throw FtcExceptionProvider.create("Event is not active");

            FtcUtils.showLeaderboard(c.getSource().asPlayer(), "crown");
            return 0;
        });
    }
}

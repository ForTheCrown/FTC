package net.forthecrown.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandCrownTop extends FtcCommand {

    public CommandCrownTop(){
        super("crowntop", CrownCore.inst());

        setPermission(Permissions.DEFAULT);
        setDescription("Shows the players with the most crown score");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUtils.showLeaderboard(c.getSource().asPlayer(), "crown");
            return 0;
        });
    }
}

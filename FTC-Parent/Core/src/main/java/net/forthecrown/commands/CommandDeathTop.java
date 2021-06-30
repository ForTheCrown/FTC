package net.forthecrown.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandDeathTop extends FtcCommand {

    public CommandDeathTop(){
        super("deathtop", CrownCore.inst());

        setPermission(Permissions.DEFAULT);
        setDescription("Shows the players with the most deaths");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUtils.showLeaderboard(getPlayerSender(c), "Death");
            return 0;
        });
    }
}

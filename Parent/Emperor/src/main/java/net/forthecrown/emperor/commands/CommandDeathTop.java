package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.utils.CrownUtils;
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

package net.forthecrown.commands;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandDeathTop extends FtcCommand {

    public CommandDeathTop(){
        super("deathtop", Crown.inst());

        setPermission(Permissions.DEFAULT);
        setDescription("Shows the players with the most deaths");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            FtcUtils.showLeaderboard(getPlayerSender(c), "Death");
            return 0;
        });
    }
}

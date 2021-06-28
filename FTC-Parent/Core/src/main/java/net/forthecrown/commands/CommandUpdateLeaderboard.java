package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.CrownCore;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandUpdateLeaderboard extends FtcCommand {
    public CommandUpdateLeaderboard(){
        super("updatelb", CrownCore.inst());
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            Pirates.leaderboard.update();

            c.getSource().sendAdmin("Leaderboard updated");
            return 0;
        });
    }
}

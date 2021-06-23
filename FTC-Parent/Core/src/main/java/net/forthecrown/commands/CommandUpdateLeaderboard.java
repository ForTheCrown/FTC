package net.forthecrown.pirates.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.pirates.Pirates;

public class CommandUpdateLeaderboard extends FtcCommand {
    public CommandUpdateLeaderboard(){
        super("updatelb", Pirates.inst);
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

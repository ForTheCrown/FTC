package net.forthecrown.pirates.commands;

import net.forthecrown.core.commands.manager.FtcCommand;
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
            Pirates.inst.updateLeaderBoard();
            c.getSource().sendMessage("Leaderboard updated");
            return 0;
        });
    }
}

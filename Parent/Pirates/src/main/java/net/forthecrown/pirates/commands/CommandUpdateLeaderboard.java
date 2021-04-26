package net.forthecrown.pirates.commands;

import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.pirates.Pirates;

public class CommandUpdateLeaderboard extends CrownCommandBuilder {
    public CommandUpdateLeaderboard(){
        super("updatelb", Pirates.inst);
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
            Pirates.inst.updateLeaderBoard();
            c.getSource().getBukkitSender().sendMessage("Leaderboard updated");
            return 0;
        });
    }
}

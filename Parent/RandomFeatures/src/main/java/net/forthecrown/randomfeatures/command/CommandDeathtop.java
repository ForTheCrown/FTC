package net.forthecrown.randomfeatures.command;

import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.randomfeatures.RandomFeatures;

public class CommandDeathtop extends CrownCommandBuilder {

    public CommandDeathtop(RandomFeatures plugin){
        super("deathtop", plugin);

        setPermission(null);
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
            RandomFeatures.instance.showLeaderboard(getPlayerSender(c), "Death");
            return 0;
        });
    }
}

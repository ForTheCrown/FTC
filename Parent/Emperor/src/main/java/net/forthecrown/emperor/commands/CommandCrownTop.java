package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandCrownTop extends CrownCommandBuilder {

    public CommandCrownTop(){
        super("crowntop", CrownCore.inst());

        setPermission((String) null);
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

package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandCrownTop extends CrownCommandBuilder {

    public CommandCrownTop(){
        super("crowntop", FtcCore.getInstance());

        setPermission(null);
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

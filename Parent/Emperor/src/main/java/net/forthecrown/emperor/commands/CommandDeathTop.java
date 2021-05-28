package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandDeathTop extends CrownCommandBuilder {

    public CommandDeathTop(){
        super("deathtop", CrownCore.inst());

        setPermission((String) null);
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

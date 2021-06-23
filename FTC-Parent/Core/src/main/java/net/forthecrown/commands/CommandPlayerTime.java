package net.forthecrown.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandPlayerTime extends FtcCommand {
    public CommandPlayerTime(){
        super("playertime", CrownCore.inst());

        setAliases("ptime");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {

    }
}

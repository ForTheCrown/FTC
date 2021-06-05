package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.FtcCommand;
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

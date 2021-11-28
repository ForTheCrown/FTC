package net.forthecrown.commands.admin;

import net.forthecrown.core.Crown;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandPlayerTime extends FtcCommand {
    public CommandPlayerTime(){
        super("playertime", Crown.inst());

        setAliases("ptime");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {

    }
}

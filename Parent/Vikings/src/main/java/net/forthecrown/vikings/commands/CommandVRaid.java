package net.forthecrown.vikings.commands;

import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.vikings.Vikings;

public class CommandVRaid extends CrownCommandBuilder {

    public CommandVRaid(){
        super("vraid", Vikings.inst());

        setPermission("ftc.vikings.admin");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
    }
}

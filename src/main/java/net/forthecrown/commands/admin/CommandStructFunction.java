package net.forthecrown.commands.admin;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.structure.FunctionInfo;

public class CommandStructFunction extends FtcCommand {
    public static final String COMMAND_NAME = "StructFunction";

    public CommandStructFunction() {
        super(COMMAND_NAME);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /StructFunction
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("args", FunctionInfo.PARSER));
    }
}
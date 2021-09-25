package net.forthecrown.commands.mail;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandMail extends FtcCommand {

    public CommandMail() {
        super("mail");

        setPermission(Permissions.MAIL);
        setDescription("Does mail stuff, idk");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Mail
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
    }
}
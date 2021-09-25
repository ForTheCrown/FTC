package net.forthecrown.commands.markets;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandMarket extends FtcCommand {

    public CommandMarket() {
        super("market");

        setPermission(Permissions.FTC_ADMIN);
        setAliases("shops");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /market
     *
     * Permissions used:
     * ftc.admin
     *
     * Main Author: Julie <3
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
    }
}
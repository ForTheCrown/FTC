package net.forthecrown.cosmetics.commands;

import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandCosmetics extends CrownCommandBuilder {

    public CommandCosmetics(){
        super("cosmetics", Cosmetics.plugin);

        setPermission(Permissions.DEFAULT);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Opens the Cosmetics menu
     *
     *
     * Valid usages of command:
     * - /cosmetics
     *
     * Author: Wout
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
           CrownUser u = getUserSender(c);
           u.getPlayer().openInventory(Cosmetics.plugin.getMainCosmeticInventory(u));
           return 0;
        });
    }
}

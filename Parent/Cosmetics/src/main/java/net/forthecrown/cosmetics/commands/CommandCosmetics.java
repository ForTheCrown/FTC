package net.forthecrown.cosmetics.commands;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandCosmetics extends CrownCommandBuilder {

    public CommandCosmetics(){
        super("cosmetics", Cosmetics.plugin);

        setPermission(null);
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

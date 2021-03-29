package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.Grave;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;

public class CommandGrave extends CrownCommandBuilder {

    public CommandGrave(){
        super("grave", FtcCore.getInstance());

        setPermission(null);
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            Grave grave = user.getGrave();

            if(grave.isEmpty()) throw new CrownCommandException("&7[FTC] Your grave is empty!");
            grave.giveItems();
            return 0;
        });
    }
}

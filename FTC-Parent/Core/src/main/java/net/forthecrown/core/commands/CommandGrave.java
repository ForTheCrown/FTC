package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.Grave;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandGrave extends FtcCommand {

    public CommandGrave(){
        super("grave", CrownCore.inst());

        setPermission(Permissions.DEFAULT);
        setDescription("Gives you the items in your grave");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            Grave grave = user.getGrave();

            if(grave.isEmpty()) throw FtcExceptionProvider.emptyGrave();
            grave.giveItems();
            return 0;
        });
    }
}

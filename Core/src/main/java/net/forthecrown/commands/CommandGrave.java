package net.forthecrown.commands;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.Grave;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandGrave extends FtcCommand {

    public CommandGrave(){
        super("grave", Crown.inst());

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

package net.forthecrown.commands.help;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class HelpRules extends FtcCommand {
    public HelpRules(){
        super("rules", CrownCore.inst());

        setDescription("Shows you the server's rules");
        setPermission(Permissions.HELP);

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            c.getSource().sendMessage(CrownCore.getRules().display());
            return 0;
        });
    }
}

package net.forthecrown.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandEavesDrop extends FtcCommand {
    public CommandEavesDrop(){
        super("eavesdrop", CrownCore.inst());

        setPermission(Permissions.EAVESDROP);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            boolean ed = user.isEavesDropping();
            ed = !ed;

            user.setEavesDropping(ed);
            c.getSource().sendAdmin(Component.text((ed ? "Now" : "No longer") + " eavesdropping").color(NamedTextColor.GRAY));
            return 0;
        });
    }
}

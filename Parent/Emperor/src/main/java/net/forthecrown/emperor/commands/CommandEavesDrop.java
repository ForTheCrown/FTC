package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandEavesDrop extends CrownCommandBuilder {
    public CommandEavesDrop(){
        super("eavesdrop", CrownCore.inst());

        setPermission(Permissions.CORE_ADMIN);
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

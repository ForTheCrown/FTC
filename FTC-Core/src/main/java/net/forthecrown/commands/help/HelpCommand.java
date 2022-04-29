package net.forthecrown.commands.help;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;

public abstract class HelpCommand extends FtcCommand {
    protected HelpCommand(String name) {
        super(name);
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            ComponentWriter writer = ComponentWriter.normal();

            writeDisplay(writer, user);

            user.sendMessage(writer.get());
            return 0;
        });
    }

    public abstract void writeDisplay(ComponentWriter writer, CrownUser user);
}
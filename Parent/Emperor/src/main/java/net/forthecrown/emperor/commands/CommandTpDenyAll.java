package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandTpDenyAll extends FtcCommand {
    public CommandTpDenyAll(){
        super("tpdenyall", CrownCore.inst());

        setPermission(Permissions.TPA);
        setDescription("Denies all incoming tpa requests");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);

            if(user.getInteractions().getCurrentIncoming().size() == 0) throw FtcExceptionProvider.noTpRequest();

            user.getInteractions().clearIncoming();
            user.sendMessage(Component.translatable("tpa.denyAll").color(NamedTextColor.YELLOW));
            return 0;
        });
    }
}

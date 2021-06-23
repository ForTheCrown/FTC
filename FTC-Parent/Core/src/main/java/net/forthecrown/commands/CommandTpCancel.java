package net.forthecrown.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandTpCancel extends FtcCommand {
    public CommandTpCancel(){
        super("tpcancel", CrownCore.inst());

        setPermission(Permissions.TPA);
        setDescription("Cancels a teleport");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);

            if(user.isTeleporting()) throw FtcExceptionProvider.create("You aren't currently teleporting");

            user.getLastTeleport().interrupt(false);
            user.sendMessage(
                    Component.translatable("tpa.cancel")
                            .color(NamedTextColor.GOLD)
            );

            return 0;
        });
    }
}

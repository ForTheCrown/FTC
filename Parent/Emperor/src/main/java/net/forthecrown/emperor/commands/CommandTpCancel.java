package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
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

package net.forthecrown.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandPayToggle extends FtcCommand {
    public CommandPayToggle(){
        super("paytoggle", CrownCore.inst());

        setDescription("Toggles your ability to pay others and for others to pay you");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    boolean accepting = !user.allowsPaying();

                    if(accepting){
                        user.sendMessage(
                                Component.translatable("commands.payToggle.on").color(NamedTextColor.YELLOW)
                        );
                    } else {
                        user.sendMessage(
                                Component.translatable("commands.payToggle.off").color(NamedTextColor.GRAY)
                        );
                    }

                    user.setAllowsPay(accepting);
                    return 0;
                });
    }
}

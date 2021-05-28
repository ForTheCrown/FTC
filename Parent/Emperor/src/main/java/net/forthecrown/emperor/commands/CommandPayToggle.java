package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandPayToggle extends CrownCommandBuilder {
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

package net.forthecrown.commands;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandPayToggle extends FtcCommand {
    public CommandPayToggle(){
        super("paytoggle", ForTheCrown.inst());

        setDescription("Toggles your ability to pay others and for others to pay you");
        setPermission(Permissions.PAY_TOGGLE);

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    boolean accepting = !user.allowsPaying();

                    user.sendMessage(
                            Component.translatable("commands.payToggle." + (accepting ? "on" : "off"))
                                    .color(accepting ? NamedTextColor.YELLOW : NamedTextColor.GRAY)
                    );

                    user.setAllowsPay(accepting);
                    return 0;
                });
    }
}

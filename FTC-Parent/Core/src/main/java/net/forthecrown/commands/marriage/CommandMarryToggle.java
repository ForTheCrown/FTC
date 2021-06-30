package net.forthecrown.commands.marriage;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandMarryToggle extends FtcCommand {

    public CommandMarryToggle() {
        super("marrytoggle", CrownCore.inst());

        setPermission(Permissions.MARRY);
        setDescription("Toggle people being able to propose to you lol");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /marrytoggle
     *
     * Permissions used:
     * ftc.marry
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    boolean accepting = !user.getInteractions().acceptingProposals();
                    String key = "marriage.toggle." + (accepting ? "on" : "off");

                    user.getInteractions().setAcceptingProposals(accepting);
                    user.sendMessage(Component.translatable(key).color(NamedTextColor.YELLOW));

                    return 0;
                });
    }
}
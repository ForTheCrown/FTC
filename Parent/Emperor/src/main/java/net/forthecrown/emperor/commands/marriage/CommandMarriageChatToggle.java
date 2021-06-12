package net.forthecrown.emperor.commands.marriage;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserInteractions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandMarriageChatToggle extends FtcCommand {

    public CommandMarriageChatToggle() {
        super("marriagechattoggle", CrownCore.inst());

        setDescription("Toggle all your chat messages being sent to marriage chat");
        setPermission(Permissions.MARRY);
        setAliases("mct", "mctoggle");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /mct
     * /mctoggle
     * /marriagechattoggle
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
                    UserInteractions inter = user.getInteractions();

                    if(inter.getMarriedTo() == null) throw FtcExceptionProvider.notMarried();

                    boolean toggled = !inter.isMarriageChatToggled();
                    String key = "marriage.chat." + (toggled ? "on" : "off");

                    inter.setMarriageChatToggled(toggled);
                    user.sendMessage(
                            Component.translatable(key).color(NamedTextColor.YELLOW)
                    );

                    return 0;
                });
    }
}
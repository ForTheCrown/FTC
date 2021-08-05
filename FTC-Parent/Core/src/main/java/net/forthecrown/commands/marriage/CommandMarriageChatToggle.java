package net.forthecrown.commands.marriage;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.UserManager;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandMarriageChatToggle extends FtcCommand {

    public CommandMarriageChatToggle() {
        super("marriagechattoggle", ForTheCrown.inst());

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

                    if(inter.getSpouse() == null) throw FtcExceptionProvider.notMarried();

                    boolean toggled = !inter.mChatToggled();

                    if(toggled){
                        CrownUser spouse = UserManager.getUser(inter.getSpouse());
                        if(!spouse.isOnline()) throw UserArgument.USER_NOT_ONLINE.create(spouse.nickDisplayName());
                    }

                    String key = "marriage.chat." + (toggled ? "on" : "off");

                    inter.setMChatToggled(toggled);
                    user.sendMessage(
                            Component.translatable(key).color(NamedTextColor.YELLOW)
                    );

                    return 0;
                });
    }
}
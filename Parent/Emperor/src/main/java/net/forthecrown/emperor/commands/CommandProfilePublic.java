package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandProfilePublic extends CrownCommandBuilder {

    protected CommandProfilePublic(CommandProfile command) {
        super("profilepublic", CrownCore.inst());

        setPermission(command.getPermission());
        setAliases("publicprofile");
        setDescription("Makes your profile public");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c ->{
            CrownUser user = getUserSender(c);
            user.setProfilePublic(true);
            user.sendMessage(Component.text("Others can now see your profile.").color(NamedTextColor.GRAY));
            return 0;
        });
    }
}

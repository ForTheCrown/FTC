package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;

public class CommandProfilePublic extends CrownCommandBuilder {

    protected CommandProfilePublic(CommandProfile command) {
        super("profilepublic", FtcCore.getInstance());

        setPermission(command.getPermission());
        setAliases("publicprofile");
        setDescription("Makes your profile public");
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c ->{
            CrownUser user = getUserSender(c);
            user.setProfilePublic(true);
            user.sendMessage("&7Others can now see your profile.");
            return 0;
        });
    }
}

package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;

public class CommandProfilePrivate extends CrownCommandBuilder {

    public CommandProfilePrivate(CommandProfile command){
        super("profileprivate", FtcCore.getInstance());

        setPermission(command.getPermission());
        setAliases("privateprofile");
        setDescription("Sets your profile to private");
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            user.setProfilePublic(false);
            user.sendMessage("&7Others can no longer see your profile.");
            return 0;
        });
    }
}

package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
            user.sendMessage(Component.text("Others can no longer see your profile.").color(NamedTextColor.GRAY));
            return 0;
        });
    }
}

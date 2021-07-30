package net.forthecrown.commands;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandProfilePrivate extends FtcCommand {

    public CommandProfilePrivate(CommandProfile command){
        super("profileprivate", ForTheCrown.inst());

        setPermission(command.getPermission());
        setAliases("privateprofile");
        setDescription("Sets your profile to private");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            user.setProfilePublic(false);
            user.sendMessage(Component.translatable("user.profile.private").color(NamedTextColor.GRAY));
            return 0;
        });
    }
}

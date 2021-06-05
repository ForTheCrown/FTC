package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandProfilePrivate extends FtcCommand {

    public CommandProfilePrivate(CommandProfile command){
        super("profileprivate", CrownCore.inst());

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

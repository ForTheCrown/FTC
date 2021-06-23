package net.forthecrown.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandProfilePublic extends FtcCommand {

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
            user.sendMessage(Component.translatable("user.profile.public").color(NamedTextColor.GRAY));
            return 0;
        });
    }
}

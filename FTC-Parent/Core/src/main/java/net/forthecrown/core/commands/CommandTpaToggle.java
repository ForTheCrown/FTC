package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandTpaToggle extends FtcCommand {
    public CommandTpaToggle(){
        super("tpatoggle", CrownCore.inst());

        setPermission(Permissions.TPA);
        setDescription("Toggles your ability to tpa to others and for others to tpa to you");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            boolean allows = user.allowsTPA();
            allows = !allows;

            if(allows) user.sendMessage(Component.translatable("tpa.toggle.on").color(NamedTextColor.YELLOW));
            else user.sendMessage(Component.translatable("tpa.toggle.off").color(NamedTextColor.GRAY));

            user.setAllowsTPA(allows);
            return 0;
        });
    }
}

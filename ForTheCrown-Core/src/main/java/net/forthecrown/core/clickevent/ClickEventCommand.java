package net.forthecrown.core.clickevent;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.CrownCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class ClickEventCommand extends CrownCommand {

    public ClickEventCommand(){
        super("npcconverse", FtcCore.getInstance());
        setDescription("The Command used by the ClickEventManager to execute code ran by clickable text");
    }

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)) return false;
        if(!ClickEventHandler.isAllowedToUseCommand((Player) sender)) return false;
        if(args.length < 1) return false;
        if(!ClickEventHandler.getRegisteredClickEvents().contains(args[0])) return false;

        ClickEventHandler.callClickEvent(args[0], args, ((Player) sender));
        return true;
    }
}

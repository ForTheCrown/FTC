package net.forthecrown.core.clickevent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClickEventCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        if(!ClickEventManager.isAllowedToUseCommand((Player) sender)) return false;
        if(args.length < 1) return false;
        if(!ClickEventManager.getRegisteredClickEvents().contains(args[0])) return false;

        String id = args[0];

        ClickEventManager.callClickEvent(id, args, ((Player) sender));
        ClickEventManager.allowCommandUsage((Player) sender, false);
        return true;
    }
}

package net.forthecrown.core.clickevent;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.CrownCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClickEventCommand extends CrownCommand {

    public ClickEventCommand(){
        super("npcconverse", FtcCore.getInstance());
        setDescription("The Command used by the ClickEventApi execute code ran by clickable text");
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        if(!ClickEventManager.isAllowedToUseCommand((Player) sender)) return false;
        if(args.length < 1) return false;
        if(!ClickEventManager.getRegisteredClickEvents().contains(args[0])) return false;

        String id = args[0];

        ClickEventManager.callClickEvent(id, args, ((Player) sender));
        return true;
    }
}

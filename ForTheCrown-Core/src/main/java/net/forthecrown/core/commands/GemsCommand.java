package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.core.files.FtcUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GemsCommand implements CrownCommandExecutor {

    public GemsCommand(){
        FtcCore.getInstance().getCommandHandler().registerCommand("gems", this);
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        FtcUser user = FtcCore.getUser(((Player) sender).getUniqueId());

        if(args.length < 1){
            user.sendMessage("&7You have &e" + user.getGems() + " Gems");
            return true;
        }

        FtcUser target;
        try {
            target = FtcCore.getUser(FtcCore.getOffOnUUID(args[0]));
        } catch (NullPointerException e){ throw new InvalidPlayerInArgument(sender, args[0]); }

        user.sendMessage("&e" + args[0] + " &7has &e" + target.getGems() + " Gems");
        return true;
    }
}

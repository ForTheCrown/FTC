package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.core.files.FtcUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GemsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        if(args.length < 1){
            FtcUser user = FtcCore.getUser(((Player) sender).getUniqueId());
            user.sendMessage("&7You have &e" + user.getGems() + " Gems");
            return true;
        }

        FtcUser user;
        try {
            user = FtcCore.getUser(FtcCore.getOffOnUUID(args[0]));
        } catch (NullPointerException e){
            throw new InvalidPlayerInArgument(sender);
        }
        return true;
    }
}

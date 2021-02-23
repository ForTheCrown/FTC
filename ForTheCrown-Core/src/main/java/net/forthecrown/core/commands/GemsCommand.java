package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class GemsCommand extends CrownCommand  {

    public GemsCommand(){
        super("gems", FtcCore.getInstance());

        setUsage("&7Usage: &r/gems <player>");
        setDescription("Shows the amount of gems you have or another player has.");
        register();
    }

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        CrownUser user = FtcCore.getUser(((Player) sender).getUniqueId());

        if(args.length < 1){
            user.sendMessage("&7You have &e" + user.getGems() + " Gems");
            return true;
        }

        CrownUser target;
        try {
            target = FtcCore.getUser(FtcCore.getOffOnUUID(args[0]));
        } catch (NullPointerException e){ throw new InvalidPlayerInArgument(sender, args[0]); }

        user.sendMessage("&e" + args[0] + " &7has &e" + target.getGems() + " Gems");
        return true;
    }
}

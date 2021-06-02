package net.forthecrown.emperor.economy;

import net.forthecrown.emperor.CrownException;
import org.bukkit.command.CommandSender;

public class BrokenShopException extends CrownException {
    public BrokenShopException(CommandSender sender){
        super(sender, "&7This shop is broken! &rPlease ask the owner to remake it");
    }
}
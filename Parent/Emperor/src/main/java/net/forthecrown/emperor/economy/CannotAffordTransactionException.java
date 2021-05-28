package net.forthecrown.emperor.economy;

import net.forthecrown.emperor.CrownException;
import org.bukkit.command.CommandSender;

public class CannotAffordTransactionException extends CrownException {

    public CannotAffordTransactionException(CommandSender sender) {
        super(sender, "&7You cannot afford that!");
    }
}

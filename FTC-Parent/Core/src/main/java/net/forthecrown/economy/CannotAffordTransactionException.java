package net.forthecrown.core.economy;

import net.forthecrown.core.CrownException;
import org.bukkit.command.CommandSender;

public class CannotAffordTransactionException extends CrownException {

    public CannotAffordTransactionException(CommandSender sender) {
        super(sender, "&7You cannot afford that!");
    }
}

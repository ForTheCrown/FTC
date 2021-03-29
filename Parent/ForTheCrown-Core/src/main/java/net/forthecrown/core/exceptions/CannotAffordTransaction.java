package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class CannotAffordTransaction extends CrownException {

    public CannotAffordTransaction(CommandSender sender) {
        super(sender, "&7You cannot afford that!");
    }

    public CannotAffordTransaction(CommandSender sender, String extraInfo) {
        super(sender, "&7You cannot afford that! &r" + extraInfo);
    }
}

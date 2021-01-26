package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class CannotAffordTransaction extends InvalidCommandExecution {

    public CannotAffordTransaction(CommandSender sender) {
        super(sender, "&cYou cannot afford that!");
    }

    public CannotAffordTransaction(CommandSender sender, String extraInfo) {
        super(sender, "&cYou cannot afford that! &r" + extraInfo);
    }
}

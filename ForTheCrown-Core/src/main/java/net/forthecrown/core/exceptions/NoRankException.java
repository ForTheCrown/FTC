package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class NoRankException extends InvalidCommandExecution{

    public NoRankException(CommandSender sender) {
        super(sender, "&7You do not have the rank requred!");
    }

    public NoRankException(CommandSender sender, String extraInfo) {
        super(sender, "&7You do not have the rank requred! &r" + extraInfo);
    }
}

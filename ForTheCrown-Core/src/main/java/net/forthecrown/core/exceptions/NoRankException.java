package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class NoRankException extends InvalidCommandExecution{

    public NoRankException(CommandSender sender) {
        super(sender, "&cYou do not have the rank requred!");
    }

    public NoRankException(CommandSender sender, String extraInfo) {
        super(sender, "&cYou do not have the rank requred! &r" + extraInfo);
    }
}

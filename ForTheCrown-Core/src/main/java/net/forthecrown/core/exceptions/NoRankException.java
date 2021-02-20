package net.forthecrown.core.exceptions;

import net.forthecrown.core.enums.Rank;
import org.bukkit.command.CommandSender;

public class NoRankException extends CrownException{

    public NoRankException(CommandSender sender) {
        super(sender, "&7You do not have the rank requred!");
    }

    public NoRankException(CommandSender sender, Rank requiredInfo) {
        super(sender, "&7You do not have the rank requred! &r" + requiredInfo.getPrefix() + " is required");
    }

    public NoRankException(CommandSender sender, String extraInfo) {
        super(sender, "&7You do not have the rank requred! &r" + extraInfo);
    }
}

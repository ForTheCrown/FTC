package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class InvalidBranchException extends CrownException {
    public InvalidBranchException(CommandSender sender) {
        super(sender, "&8Wrong branch!");
    }

    public InvalidBranchException(CommandSender sender, String correctBranch) {
        super(sender, "&8Wrong branch! &7 Only " + correctBranch + "s are able to do this!");
    }

    public InvalidBranchException(CommandSender sender, String correctBranch, String extraInfo) {
        super(sender, "&8Wrong branch! &7 Only " + correctBranch + "s are able to do this! &r" + extraInfo);
    }
}

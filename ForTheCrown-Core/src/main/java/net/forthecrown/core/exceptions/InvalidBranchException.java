package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class InvalidBranchException extends InvalidCommandExecution {
    public InvalidBranchException(CommandSender sender) {
        super(sender, "&cWrong branch!");
    }
    public InvalidBranchException(CommandSender sender, String correctBranch) {
        super(sender, "&cWrong branch! &7 Only " + correctBranch + "s are able to do this!");
    }
}

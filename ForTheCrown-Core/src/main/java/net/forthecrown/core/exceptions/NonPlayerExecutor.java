package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class NonPlayerExecutor extends InvalidCommandExecution {

    public NonPlayerExecutor(CommandSender sender){
        super(sender, "&7Only players may execute this command!");
    }
}

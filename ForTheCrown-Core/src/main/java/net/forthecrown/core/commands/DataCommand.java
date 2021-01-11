package net.forthecrown.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DataCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }
}

//TODO getters and setters for everything in the FtcUser class
//TODO resetPrices arg to reset all prices and amountEarned

package net.forthecrown.core;

import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CrownCommandExecutor {
    boolean run(CommandSender sender, Command command, String label, String[] args) throws CrownException;
}
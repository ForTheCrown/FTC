package net.forthecrown.core;

import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

public interface CrownCommandExecutor {
    boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException;
}
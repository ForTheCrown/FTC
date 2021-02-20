package net.forthecrown.pirates.commands;

import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.pirates.Pirates;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

public class PirateReload extends CrownCommand {
    public PirateReload() {
        super("rlpirate", Pirates.plugin);

        setPermission("ftc.pirates.admin");
        register();
    }

    @Override
    public boolean run(@Nonnull CommandSender sender,@Nonnull Command command,@Nonnull String label,@Nonnull String[] args) throws CrownException {
        Pirates.plugin.reloadConfig();
        Pirates.plugin.updateDate();
        sender.sendMessage(ChatColor.GRAY + "Pirate config reloaded.");
        return true;
    }
}

package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

public class Discord extends CrownCommand {
    public Discord(){
        super("Discord", FtcCore.getInstance());

        setPermission(null);
        setDescription("Gives you the servers discord link.");
        register();
    }

    /*
    * Just sends the player the discord link
     */

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
        sender.sendMessage(FtcCore.getPrefix() + FtcCore.getDiscord());
        return true;
    }
}

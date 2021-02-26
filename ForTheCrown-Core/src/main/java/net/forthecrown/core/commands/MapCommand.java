package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

public class MapCommand extends CrownCommand {
    public MapCommand(){
        super("map", FtcCore.getInstance());

        setAliases("worldmap");
        setPermission(null);

        register();
    }

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
        sender.sendMessage(ChatColor.GRAY + "Dynmap link:");
        sender.sendMessage(ChatColor.AQUA +"http://mc.forthecrown.net:3140/");
        return true;
    }
}

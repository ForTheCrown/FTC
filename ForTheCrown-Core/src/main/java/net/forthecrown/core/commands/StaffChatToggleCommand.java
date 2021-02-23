package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Set;

public class StaffChatToggleCommand extends CrownCommand {

    public StaffChatToggleCommand(){
        super("staffchattoggle", FtcCore.getInstance());

        setAliases("sct", "sctoggle");
        setPermission("ftc.staffchat");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Toggles a players every message going to staffchat
     *
     *
     * Valid usages of command:
     * - /staffchattoggle
     * - /sct
     *
     * Permissions used:
     * - ftc.staffchat
     *
     * Author: Botul
     */

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players may execute this command!");
            return false;
        }

        Player player = (Player) sender;
        Set<Player> set = FtcCore.getSCTPlayers();
        String staffPrefix = ChatColor.DARK_GRAY + "[Staff] ";

        if(set.contains(player)){
            set.remove(player);
            player.sendMessage(staffPrefix + ChatColor.GRAY + "Your messages will no longer all go to staffchat");
        } else{
            set.add(player);
            player.sendMessage(staffPrefix + ChatColor.GRAY + "Your messages will now all go to staffchat");
        }

        FtcCore.setSCTPlayers(set);
        return true;
    }
}

package net.forthecrown.core.chat.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.files.FtcUser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleEmotes implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Adds player to config list "NoEmotes", which
     * disables receiving and sending these emotes:
     * 	- mwah
     * 	- poke
     * 	- bonk
     *
     *
     * Valid usages of command:
     * - /toggleemotes
     *
     *
     * Referenced other classes:
     * - FtcUserData
     * - FtcCore
     *
     * Main Author: Botul
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players may execute this command!");
            return false;
        }

        Player player = (Player) sender;
        FtcUser userData = FtcCore.getUserData(player.getUniqueId());
        String message = ChatColor.GRAY + "You can longer send or recieve emotes.";

        if(userData.getAllowsEmotes()) userData.setAllowsEmotes(false);
        else {
            userData.setAllowsEmotes(true);
            message = ChatColor.YELLOW + "You can now send and recieve emotes.";
        }

        player.sendMessage(message);
        return true;
    }
}

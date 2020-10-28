package ftc.chat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ftc.chat.Main;

import java.util.List;

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
	 * - Main: Main.plugin
	 * - ...
	 * 
	 * Main Author: Botul
	 * Edit by: Wout
	 */
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	// Sender must be player:
    	if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can do this.");
			return false;
		}
        
        Player player = (Player) sender;
        List<String> listOfNoEmotePeople = Main.plugin.getConfig().getStringList("NoEmotes");

        // Toggle playerUUID being in the list:
        if (listOfNoEmotePeople.contains(player.getUniqueId().toString())) {
        	listOfNoEmotePeople.remove(player.getUniqueId().toString());
            player.sendMessage(ChatColor.YELLOW + "You can now send and receive emotes.");
        } 
        else {
        	listOfNoEmotePeople.add(player.getUniqueId().toString());
            player.sendMessage(ChatColor.GRAY + "You can no longer send or receive emotes.");
        }

        // Save list:
        Main.plugin.getConfig().set("NoEmotes", listOfNoEmotePeople);
        Main.plugin.saveConfig();

        return true;
    }
}
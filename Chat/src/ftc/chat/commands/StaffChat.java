package ftc.chat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffChat implements CommandExecutor {
	
	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Sends a message to all players with the ftc.staffchat permission.
	 * 
	 * 
	 * Valid usages of command:
	 * - /sc [message]
	 * 
	 * Permissions used:
	 * - ftc.staffchat
	 * 
	 * Main Author: Botul
	 * Edit by: Wout
	 */
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	// Command args:
        if(args.length < 1) {
        	sender.sendMessage(ChatColor.GRAY + "Usage: /sc <Message>");
        	return false;
        }

        // Construct message:
        String message = "";
        for (int i = 0; i < args.length ; i++) { 
            message += " ";
            message += args[i];
        }
        if (message.contains(":")) {
        	message = message.replaceAll(":shrug:", "¯\\\\_(ツ)_/¯");
    		message = message.replaceAll(":ughcry:", "(ಥ﹏ಥ)");
    		message = message.replaceAll(":gimme:", "༼ つ ◕_◕ ༽つ");
    		message = message.replaceAll(":gimmecry:", "༼ つ ಥ_ಥ ༽つ");
    		message = message.replaceAll(":bear:", "ʕ• ᴥ •ʔ");
    		message = message.replaceAll(":smooch:", "( ^ 3^) ♥");
    		message = message.replaceAll(":why:", "ლ(ಠ益ಠლ)");
    		message = message.replaceAll(":tableflip:", "(ノಠ益ಠ)ノ彡┻━┻");
    		message = message.replaceAll(":tableput:", " ┬──┬ ノ( ゜-゜ノ)");
    		message = message.replaceAll(":pretty:", "(◕‿◕ ✿)");
    		message = message.replaceAll(":sparkle:", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧");
    		message = message.replaceAll(":blush:", "(▰˘◡˘▰)");
    		message = message.replaceAll(":sad:", "(._. )");
        }
		
        // Send message to all staff players
        for (Player p : Bukkit.getServer().getOnlinePlayers()) { 
            if(p.hasPermission("ftc.staffchat"))  p.sendMessage(ChatColor.translateAlternateColorCodes('&', ("&7[Staff] " + sender.getName() + " &l>&r" + message)));
        }
        return false;
    }
}

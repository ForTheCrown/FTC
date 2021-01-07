package ftc.dataplugin.commands;

import java.util.List;
import java.util.UUID;

import ftc.dataplugin.DataPlugin;
import ftc.dataplugin.FtcUserData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class setbranch implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * sets a player branch
	 *
	 *
	 * Valid usages of command:
	 * - /setbranch <player> <branch>
	 *
	 * Permissions used:
	 * - ftc.setbranch
	 *
	 * Referenced other classes:
	 * - DataPlugin: DataPlugin.plugin
	 *
	 * Author: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		// Valid use of command
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED + "/setbranch [player] [empty/Knight/Pirate/Viking]");
			return false;
		}
		
		// Valid player
		UUID playeruuid = DataPlugin.trySettingUUID(args[0]);
		if (playeruuid == null) {
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid playername.");
			return false;
		}
		FtcUserData userData = DataPlugin.getUserData(Bukkit.getPlayer(args[0]));
		
		// Valid branch
		List<String> acceptedBranches = DataPlugin.getInstance().getPossibleBranches();
		if (!acceptedBranches.contains(args[1])) {
			sender.sendMessage(ChatColor.RED + "Invalid branch, use of one these:");
			String message = ChatColor.GRAY + "";
			for (String branch : acceptedBranches)
				message += branch + ", ";
			sender.sendMessage(message);
			return false;
		}
		
		// Check if CanSwapBranch allows changing
		if (!userData.getCanSwapBranch()) {
			sender.sendMessage(ChatColor.GRAY + "This player can't swap branches right now because CanSwapBranch is false.");
			return false;
		}
		
		// Change branch
		String oldBranch = userData.getActiveBranch();
		userData.setActiveBranch(args[1]);
		userData.saveUserData();
		sender.sendMessage(ChatColor.GRAY + "Changed the Active Branch of " + args[0] + " from " + oldBranch + " to " + args[1]);
		return true;
	}

}

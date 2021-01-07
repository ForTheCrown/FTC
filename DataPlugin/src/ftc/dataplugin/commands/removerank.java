package ftc.dataplugin.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ftc.dataplugin.DataPlugin;
import ftc.dataplugin.FtcUserData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class removerank implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Removes a players rank
	 *
	 *
	 * Valid usages of command:
	 * - /removerank <Player> <Rank>
	 *
	 * Permissions used:
	 * - ftc.removerank
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
			sender.sendMessage(ChatColor.RED + "/removerank [player] [rank]");
			return false;
		}
		
		// Valid rank
		String[] temp = {"knight", "baron", "sailor", "pirate"};
		List<String> acceptedRanks = new ArrayList<>(Arrays.asList(temp));
		if (!acceptedRanks.contains(args[1])) {
			sender.sendMessage(ChatColor.RED + "Invalid rank, use of one these:");
			String message = ChatColor.GRAY + "";
			for (String rank : temp)
				message += rank + ", ";
			sender.sendMessage(message);
			return false;
		}
		
		// Valid player
		UUID playeruuid = DataPlugin.trySettingUUID(args[0]);
		if (playeruuid == null) {
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid playername.");
			return false;
		}
		FtcUserData userData = DataPlugin.getUserData(Bukkit.getPlayer(args[0]));

		
		// Remove rank
		if (args[1].contains("knight") || args[1].contains("baron")) {
			List<String> temp2 = userData.getKnightRanks();
			if (!temp2.contains(args[1])){
				sender.sendMessage(ChatColor.GRAY + args[0].toLowerCase() + " doesn't have the " + args[1] + " rank.");
				return false;
			}
			temp2.remove(args[1]);
			userData.setKnightRanks(temp2);
		}
		else if (args[1].contains("sailor") || args[1].contains("pirate")) {
			List<String> temp2 = userData.getPirateRanks();
			if (!temp2.contains(args[1])) {
				sender.sendMessage(ChatColor.GRAY + args[0].toLowerCase() + " doesn't have the " + args[1] + " rank.");
				return false;
			}
			temp2.remove(args[1]);
			userData.setPirateRanks(temp2);
		}

		sender.sendMessage(ChatColor.GRAY + "Removed " + args[1] + " from their rank set.");
		
		userData.setCurrentRank("default");
		userData.saveUserData();
		Bukkit.dispatchCommand(DataPlugin.getInstance().getServer().getConsoleSender(), "tab player " + args[0] + " tabprefix");
		
		return true;
	}

}

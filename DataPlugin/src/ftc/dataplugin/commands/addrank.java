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

public class addrank implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Explain what command is supposed to be used for..
	 *
	 *
	 * Valid usages of command:
	 * - /addrank <player> <rank>
	 *
	 * Permissions used:
	 * - ftc.addrank
	 *
	 * Referenced other classes:
	 * - DataPlugin: DataPlugin.plugin
	 *
	 * Author: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Permission
		if (!sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return false;
		}
		
		// Valid use of command
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED + "/addrank [player] [rank]");
			return false;
		}
		
		// Valid rank
		String[] temp = {"knight", "baron", "sailor", "pirate"};
		List<String> acceptedRanks = new ArrayList<>(Arrays.asList(temp));
		if (!acceptedRanks.contains(args[1])) 
		{
			sender.sendMessage(ChatColor.RED + "Invalid rank, use of one these:");
			String message = ChatColor.GRAY + "";
			for (String rank : temp)
				message += rank + ", ";
			sender.sendMessage(message);
			return false;
		}
		
		// Valid player
		UUID playeruuid = DataPlugin.trySettingUUID(args[0]);
		if (playeruuid == null)
		{
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid playername.");
			return false;
		}

		FtcUserData targetData = DataPlugin.getUserData(Bukkit.getPlayer(args[0]));
		
		// Add rank
		if (args[1].contains("knight") || args[1].contains("baron"))
		{
			List<String> temp2 = targetData.getKnightRanks();
			if (temp2.contains(args[1]))
			{
				sender.sendMessage(ChatColor.GRAY + args[0].toLowerCase() + " already has the " + args[1] + " rank.");
				return false;
			}
			temp2.add(args[1]);
			targetData.setKnightRanks(temp2);
		}
		else if (args[1].contains("sailor") || args[1].contains("pirate"))
		{
			List<String> temp2 = targetData.getPirateRanks();
			if (temp2.contains(args[1]))
			{
				sender.sendMessage(ChatColor.GRAY + args[0].toLowerCase() + " already has the " + args[1] + " rank.");
				return false;
			}
			temp2.add(args[1]);
			targetData.setPirateRanks(temp2);
		}

		targetData.saveUserData();
		sender.sendMessage(ChatColor.GRAY + "Added " + args[1] + " to their rank set.");
		return true;
	}

}

package ftc.dataplugin.commands;

import ftc.dataplugin.DataPlugin;
import ftc.dataplugin.FtcUserData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class getbranch implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Tells you what branch a player is in
	 *
	 *
	 * Valid usages of command:
	 * - /getbranch <player>
	 *
	 * Permissions used:
	 * - ftc.getbranch
	 *
	 * Referenced other classes:
	 * - DataPlugin: DataPlugin.plugin
	 *
	 * Author: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Valid use of command
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "/getbranch [player]");
			return false;
		}
		
		// Valid player
		UUID playeruuid = DataPlugin.trySettingUUID(args[0]);
		if (playeruuid == null)
		{
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid playername.");
			return false;
		}

		FtcUserData data = DataPlugin.getUserData(Bukkit.getPlayer(args[0]));
		
		// Return value
		sender.sendMessage(ChatColor.GRAY + "" + data.getActiveBranch());
		return true;
	}

}

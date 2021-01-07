package ftc.dataplugin.commands;

import ftc.dataplugin.DataPlugin;
import ftc.dataplugin.FtcUserData;
import net.minecraft.server.v1_16_R3.DataPaletteLinear;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class setswapbranch implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * sets if a player is allowed to swap branches
	 *
	 *
	 * Valid usages of command:
	 * - /setswapbranch <player> <true | false>>
	 *
	 * Permissions used:
	 * - ftc.setsawpbranch
	 *
	 * Referenced other classes:
	 * - DataPlugin: DataPlugin.plugin
	 *
	 * Author: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Valid use of command
		if ((args.length != 2) || (!(args[1].matches("true") || args[1].matches("false")))) {
			sender.sendMessage(ChatColor.RED + "/setswapbranch [player] [true/false]");
			return false;
		}
		
		// Valid player
		UUID playeruuid = DataPlugin.trySettingUUID(args[0]);
		if (playeruuid == null) {
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid playername.");
			return false;
		}
		FtcUserData userData = DataPlugin.getUserData(Bukkit.getPlayer(args[0]));

		
		// Set value
		userData.setCanSwapBranch(args[1].matches("true"));
		userData.saveUserData();
		sender.sendMessage(ChatColor.GRAY + "Set the value of canswapbranch of " + args[0] + " to " + args[1]);
		return true;
	}

}

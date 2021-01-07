package ftc.dataplugin.commands;

import ftc.dataplugin.DataPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.UUID;

public class MakeBaron implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Makes a player a baron
	 *
	 *
	 * Valid usages of command:
	 * - /makebaron <Player>
	 *
	 * Permissions used:
	 * - ftc.makebaron
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
			sender.sendMessage(ChatColor.RED + "/makebaron [player]");
			return false;
		}
		
		// Valid player
		UUID playeruuid = DataPlugin.trySettingUUID(args[0]);
		if (playeruuid == null)
		{
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid playername.");
			return false;
		}
		
		Bukkit.dispatchCommand(DataPlugin.getInstance().getServer().getConsoleSender(), "addrank " + args[0] + " baron");
		sender.sendMessage(ChatColor.GRAY + "Added baron to their rank set.");
		
		Objective baron = Bukkit.getServer().getScoreboardManager().getMainScoreboard().getObjective("Baron");
		Score baronPoints = baron.getScore(args[0]);
		baronPoints.setScore(1);
		
		return true;
	}

}

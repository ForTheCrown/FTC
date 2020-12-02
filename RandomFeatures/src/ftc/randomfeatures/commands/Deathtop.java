package ftc.randomfeatures.commands;

import ftc.randomfeatures.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;


public class Deathtop implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Shows a leaderboard of deaths to the player that executes the command
	 *
	 *
	 * Valid usages of command:
	 * - /deathtop
	 *
	 * Permissions used:
	 * - NONE
	 *
	 * Referenced other classes:
	 * - Main: Main.plugin
	 *
	 * Author: Wout
	 */
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		// Checks if sender is a player.
		if (!(sender instanceof Player)) {
			return false;
		}
		
		Player player = (Player) sender;
		Scoreboard mainScoreboard = Main.plugin.getServer().getScoreboardManager().getMainScoreboard();
		Objective objective = mainScoreboard.getObjective("Death");
		
		Scoreboard scoreboard = Main.plugin.getServer().getScoreboardManager().getNewScoreboard();
		Objective newObj = scoreboard.registerNewObjective(player.getName(), "dummy");
		newObj.setDisplayName(ChatColor.GOLD + "---" + ChatColor.YELLOW + "Leaderboard" + ChatColor.GOLD + "---");
		
		for(String name : objective.getScoreboard().getEntries()) {
			newObj.getScore(name).setScore(objective.getScore(name).getScore());
		}
		
		newObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		player.setScoreboard(scoreboard);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        @Override
	        public void run() {
	        	player.setScoreboard(mainScoreboard);
	        }
	    }, 300L);
		
		return true;
	}
}
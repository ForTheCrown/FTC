package me.wout.Pirate.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.wout.Pirate.Main;

public class UpdateLB implements CommandExecutor {

	public UpdateLB() {
		Main.plugin.getCommand("updatelb").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		if (!sender.isOp())
		{
			sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
			return false;
		}
		Main.plugin.updateLeaderBoard();
		return true;
	}
}

package me.wout.Pirate.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.wout.Pirate.Main;

public class ghtargetshowname implements CommandExecutor {
	
	public ghtargetshowname() {
		Main.plugin.getCommand("GHTargetShowName").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("Only players can execute this command.");
			return false;
		}
		
		Player player = (Player) sender;
		if (!player.isOp())
		{
			player.sendMessage(ChatColor.RED + "You don't have permission to do this.");
			return false;
		}
		
		for (Entity nearbyEntity : player.getWorld().getNearbyEntities(player.getLocation(), 2, 2, 2))
		{
			if (nearbyEntity.getType() != EntityType.ARMOR_STAND) continue;
			
			if (nearbyEntity.getCustomName() != null && nearbyEntity.getCustomName().contains("GHTargetStand"))
			{
				if (nearbyEntity.isCustomNameVisible()) nearbyEntity.setCustomNameVisible(false);
				else nearbyEntity.setCustomNameVisible(true);
				return true;				
			}
		}
		return false;
	}
}

package me.wout.RandomFeatures.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

//import ftc.name.Main;

public class ChristmasGift implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Copies the shulker box from the chest and gives it to the player
	 * 
	 * 
	 * Valid usages of command:
	 * - /christmasgift <player> 
	 * 
	 * Permissions used:
	 * - OP
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * 
	 * Author: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Sender must be opped:
		if (!sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return false;
		}
		
		// Get player:
		if(args.length < 1) {
        	sender.sendMessage(ChatColor.GRAY + "Usage: /christmasgift <player>");
        	return false;
        }
		Location targetLoc;
		try {
			targetLoc = Bukkit.getPlayer(args[0]).getLocation();
		} catch (Exception notOnline) {
			sender.sendMessage(ChatColor.GRAY + "Couldn't get " + args[0]);
			return false;
		}
		
		// Get shulker:
		Location chestLoc = new Location(Bukkit.getWorld("world"), 278, 80, 964);
		Chest chest;
		try {
			chest = (Chest) chestLoc.getBlock().getState();
		}
		catch (Exception e) {
			sender.sendMessage("Not a chest at location: 'world', 278, 80, 964");
			return false;
		}
		ItemStack shulker = chest.getInventory().getItem(0);
		if (shulker == null) {
			sender.sendMessage("Not an item in slot 0 in chest at location: 'world', 278, 80, 964");
			return false;
		}
		
		Item giftBox = targetLoc.getWorld().dropItem(targetLoc, shulker);
		giftBox.setVelocity(new Vector(0, 0.2, 0));
		sender.sendMessage("Gave Christmas giftbox to " + args[0] + " :D");

		return true;
	}
}

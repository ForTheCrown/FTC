package me.wout.RandomFeatures.commands;

import org.bukkit.command.CommandExecutor;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.wout.RandomFeatures.Main;

public class wild implements CommandExecutor{

	private Set<String> onCooldown = new HashSet<String>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Checks if sender is a player.
		if (!(sender instanceof Player)) 
		{
			sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
			return false;
		}
		
		Player p = (Player) sender;
		if (!p.getWorld().getName().equalsIgnoreCase("world_resource"))
		{
			sender.sendMessage(ChatColor.GRAY + "You can only do this in the resource world.");
			sender.sendMessage(ChatColor.GRAY + "The portal to get there is in Hazelguard.");
			return false;
		}
		
		if (onCooldown.contains(p.getName()))
		{
			sender.sendMessage(ChatColor.GRAY + "You can only do this command every 30 seconds.");
			return false;
		}
		
		int x = 0;
		if (Math.random() < 0.5)
			x = getRandomNumberInRange(200, 1800);
		else 
			x = getRandomNumberInRange(-1800, -200);
		
		int z = 0;
		if (Math.random() < 0.5)
			z = getRandomNumberInRange(200, 1800);
		else 
			z = getRandomNumberInRange(-1800, -200);
		
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 400, 1));
		p.teleport(new Location(Bukkit.getWorld("world_resource"), x, 150, z));
		
		p.sendMessage(ChatColor.GRAY + "You've been teleported, do " + ChatColor.YELLOW + "/warp portal" + ChatColor.GRAY + " to get back.");
		
		startCooldown(p.getName());
		
		return true;
	}
	
	private static int getRandomNumberInRange(int min, int max) {
		if (min >= max) {
			return 0;
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
	private void startCooldown(String playername) {
		onCooldown.add(playername);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			@Override
			public void run() {
				onCooldown.remove(playername);
			}
		}, 600L);
		
	}
	
}




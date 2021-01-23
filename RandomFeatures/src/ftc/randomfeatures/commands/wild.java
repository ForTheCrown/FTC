package ftc.randomfeatures.commands;

import ftc.randomfeatures.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class wild implements CommandExecutor{

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Teleports the player into the wild if they're in the RW
	 * or tells them to sod off if they're in any other world.
	 * Console can bypass the world restriction
	 *
	 *
	 * Valid usages of command:
	 * - /wild
	 * - /wild <player name> If executor is console
	 *
	 * Permissions used:
	 * - NONE
	 *
	 * Referenced other classes:
	 * - Main: Main.plugin
	 *
	 * Author: Wout
	 * Editor: Botul
	 */

	private Set<String> onCooldown = new HashSet<String>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Checks if sender is a player.
		if (sender instanceof Player) {

			Player p = (Player) sender;
			if (!p.getWorld().getName().equalsIgnoreCase("world_resource")) {
				sender.sendMessage(ChatColor.GRAY + "You can only do this in the resource world.");
				sender.sendMessage(ChatColor.GRAY + "The portal to get there is in Hazelguard.");
				return false;
			}

			if (onCooldown.contains(p.getName())) {
				sender.sendMessage(ChatColor.GRAY + "You can only do this command every 30 seconds.");
				return false;
			}

			wildTP(p);
			return true;
		}

		//console sender
		if(args.length < 1){
			sender.sendMessage("Too little arguments");
			return false;
		}
		Player target = Bukkit.getPlayer(args[0]);
		if(target == null) return false;
		wildTP(target);
		
		return true;
	}

	public void wildTP(Player p){
		int x = 0;
		if (Math.random() < 0.5)
			x = Main.getRandomNumberInRange(200, 1800);
		else
			x = Main.getRandomNumberInRange(-1800, -200);

		int z = 0;
		if (Math.random() < 0.5)
			z = Main.getRandomNumberInRange(200, 1800);
		else
			z = Main.getRandomNumberInRange(-1800, -200);

		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 400, 1));
		p.teleport(new Location(p.getWorld(), x, 150, z));

		if(p.getWorld().getName().contains("world_resouce")) p.sendMessage(ChatColor.GRAY + "You've been teleported, do " + ChatColor.YELLOW + "/warp portal" + ChatColor.GRAY + " to get back.");

		startCooldown(p.getName());
	}
	
	private void startCooldown(String playername) {
		onCooldown.add(playername);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> onCooldown.remove(playername), 600L);
	}
}




package me.wout.RandomFeatures.commands;

import org.bukkit.command.CommandExecutor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class Capture implements CommandExecutor{

	//private main plugin;
	//Location loc;
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		/*if (sender instanceof Player) {
			sender.sendMessage(ChatColor.RED + "Players can't do this.");
			return false;
		}
		if (args.length != 3) {
			sender.sendMessage(ChatColor.RED + "[Capture] Invalid use:" + ChatColor.RESET + "/capture x y z");
			return false;
		}
		if (!(isNumeric(args[0]) && isNumeric(args[1]) && isNumeric(args[2]))) {
			sender.sendMessage(ChatColor.RED + "[Capture] Invalid use:" + ChatColor.RESET + "/capture x y z");
			return false;
		}
		
		int x = Integer.parseInt(args[0]);
		int y = Integer.parseInt(args[1]);
		int z = Integer.parseInt(args[2]);
		this.loc = new Location(Bukkit.getWorld("world"), x, y, z);
		
		
		List<Entity> nearbyEntites = (List<Entity>) loc.getWorld().getNearbyEntities(loc, 1.0, 2, 1.0);
		for (Entity ent : nearbyEntites) {
			if (ent.getType() != EntityType.PLAYER) {
				nearbyEntites.remove(ent);
			}
		}
		if (nearbyEntites.isEmpty()) {
			sender.sendMessage("[Capture] Didn't find any players.");
			return false;
		}
		else if (nearbyEntites.size() != 1) {
			sender.sendMessage("[Capture] Not one entity.");
			return false;
		}
		
		if (plugin.capturePointHealth == null) {
			createCapturePointBossBar(((Player) nearbyEntites.get(0)).getName());
			plugin.state = 1;
		}
		else {
			plugin.capturePointHealth.addPlayer(((Player) nearbyEntites.get(0)));
			plugin.state = 3;
		}
		*/
		return true;
	}
	
	/*public void createCapturePointBossBar(String playerName) {
		plugin.capturePointHealth = Bukkit.createBossBar("Captured by: " + ChatColor.YELLOW + playerName, BarColor.YELLOW, BarStyle.SEGMENTED_12);
		plugin.capturePointHealth.setProgress(0.006);
		plugin.capturePointHealth.addPlayer(Bukkit.getPlayer(playerName));
		
		List<Entity> nearbyEntites2 = (List<Entity>) loc.getWorld().getNearbyEntities(loc, 40, 80, 40);
		for (Entity ent : nearbyEntites2) {
			if (ent instanceof Player) {
				plugin.capturePointHealth.addPlayer((Player) ent);
			}
		}
		plugin.playername = playerName;
	}
	

	
	private static boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        @SuppressWarnings("unused")
			int d = Integer.parseInt(strNum);
	    } catch (NumberFormatException e) {
	        return false;
	    }
	    return true;
	}
	
	public Location getLocation() {
		return this.loc;
	}*/
}

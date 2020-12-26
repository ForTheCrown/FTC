package ftc.cosmetics.commands;

import org.bukkit.command.CommandExecutor;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ftc.cosmetics.Main;
import net.md_5.bungee.api.ChatColor;

public class AddParticle implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Adds a Particle to a player
	 * 
	 * 
	 * Valid usages of command:
	 * - /addparticle [player] [arrow/death/emote] [particle]
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * - DataPlugin: configfile
	 * 
	 * Author: Wout
	 */
	
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Sender must be opped:
		if (!sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return false;
		}
		
		// Valid use of command:
		if (args.length != 3) {
			sender.sendMessage(ChatColor.RED + "/addparticle [player] [arrow/death/emote] [particle]");
			return false;
		}
		
		// Try getting uuid of given arg:
		String targetUuid;
		try {
			targetUuid = Bukkit.getPlayer(args[0]).getUniqueId().toString();
		}
		catch (Exception e) {
			try {
				targetUuid = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
			}
			catch (Exception e2) {
				sender.sendMessage(ChatColor.GRAY + args[0] + " is not a valid player.");
				return false;
			}
		}
		if (!Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getConfigurationSection("players").getKeys(false).contains(targetUuid)) {
			sender.sendMessage(ChatColor.GRAY + args[0] + " not found in dataplugin config.");
			return false;
		}
		
		// Found matching uuid but names do not match, update name:
		if (!Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + targetUuid + ".PlayerName").equalsIgnoreCase(args[0]))
		{
			Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + targetUuid + ".PlayerName", args[0]);
		}
		
		
		switch (args[1]) {
		case "arrow":
		{
			List<String> availableEffects = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + targetUuid + ".ParticleArrowAvailable");
			
			if (availableEffects.contains(args[2])) {
				sender.sendMessage(ChatColor.GRAY + "This player already has this particle.");
				return false;
			}
			else if (!Main.plugin.getAcceptedArrowParticles().contains(args[2])) {
				sender.sendMessage(ChatColor.GRAY + "Use one of these particles:");
				String message = ChatColor.GRAY + "";
				for (String particle : Main.plugin.getAcceptedArrowParticles()) {
					message += particle + ", ";
				}
				sender.sendMessage(message);
				return false;
			}
			else {
				availableEffects.add(args[2]);
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + targetUuid + ".ParticleArrowAvailable", availableEffects);
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").saveConfig();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Added &f" + args[2] + "&7 to &f" + args[0] + "&7's arrow-particles."));
				return true;
			}
		}
		case "death":
		{
			List<String> availableEffects = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + targetUuid + ".ParticleDeathAvailable");
			
			if (availableEffects.contains(args[2])) {
				sender.sendMessage(ChatColor.GRAY + "This player already has this particle.");
				return false;
			}
			else if (!Main.plugin.getAcceptedDeathParticles().contains(args[2])) {
				sender.sendMessage(ChatColor.GRAY + "Use one of these particles:");
				String message = ChatColor.GRAY + "";
				for (String particle : Main.plugin.getAcceptedDeathParticles()) {
					message += particle + ", ";
				}
				sender.sendMessage(message);
				return false;
			}
			else {
				availableEffects.add(args[2]);
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + targetUuid + ".ParticleDeathAvailable", availableEffects);
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").saveConfig();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Added &f" + args[2] + "&7 to &f" + args[0] + "&7's death-particles."));
				return true;
			}
		}
		case "emote":
		{
			List<String> availableEmotes = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + targetUuid + ".EmotesAvailable");
			
			if (availableEmotes.contains(args[2])) {
				sender.sendMessage(ChatColor.GRAY + "This player already has this emote.");
				return false;
			}
			else if (!Main.plugin.getAcceptedEmotes().contains(args[2])) {
				sender.sendMessage(ChatColor.GRAY + "Use one of these emotes:");
				String message = ChatColor.GRAY + "";
				for (String emote : Main.plugin.getAcceptedEmotes()) {
					message += emote + ", ";
				}
				sender.sendMessage(message);
				return false;
			}
			else {
				availableEmotes.add(args[2]);
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + targetUuid + ".EmotesAvailable", availableEmotes);
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").saveConfig();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Added &f" + args[2] + "&7 to &f" + args[0] + "&7's emotes."));
				return true;
			}
			
		}
		default: 
			sender.sendMessage(ChatColor.RED + "/addparticle [player] [arrow/death/emote] [particle]");
			return false;
		}
	}
}

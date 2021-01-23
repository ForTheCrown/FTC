package ftc.cosmetics.commands;

import org.bukkit.command.CommandExecutor;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ftc.cosmetics.Main;
import net.md_5.bungee.api.ChatColor;

public class RemoveParticle implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Removes a Particle from a player.
	 * 
	 * 
	 * Valid usages of command:
	 * - /removeparticle [player] [arrow/death/emote] [particle]
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
		// Valid use of command:
		if (args.length != 3) return false;
		
		switch (args[1]) {
		case "arrow":
		{
			List<String> availableEffects = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + targetUuid + ".ParticleArrowAvailable");

			if (!Main.plugin.getAcceptedArrowParticles().contains(args[2])) {
				sender.sendMessage(ChatColor.GRAY + "Use one of these particles:");
				String message = ChatColor.GRAY + "";
				for (String particle : Main.plugin.getAcceptedArrowParticles()) {
					message += particle + ", ";
				}
				sender.sendMessage(message);
				return false;
			}
			if (availableEffects.contains(args[2])) {
				availableEffects.remove(args[2]);
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + targetUuid + ".ParticleArrowAvailable", availableEffects);
				if (Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + targetUuid + ".ParticleArrowActive").contains(args[2]))
					Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + targetUuid + ".ParticleArrowActive", "none");
				
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").saveConfig();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Removed &f" + args[2] + "&7 from &f" + args[0] + "&7's arrow-particles."));
				return true;
			}
			else {
				sender.sendMessage(ChatColor.GRAY + "This player doesn't have this particle.");
				return false;
			}
		}
		case "death":
		{
			List<String> availableEffects = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + targetUuid + ".ParticleDeathAvailable");

			if (!Main.plugin.getAcceptedDeathParticles().contains(args[2])) {
				sender.sendMessage(ChatColor.GRAY + "Use one of these particles:");
				String message = ChatColor.GRAY + "";
				for (String particle : Main.plugin.getAcceptedDeathParticles()) {
					message += particle + ", ";
				}
				sender.sendMessage(message);
				return false;
			}
			if (availableEffects.contains(args[2])) {
				availableEffects.remove(args[2]);
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + targetUuid + ".ParticleDeathAvailable", availableEffects);
				if (Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + targetUuid + ".ParticleDeathActive").contains(args[2]))
					Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + targetUuid + ".ParticleDeathActive", "none");
				
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").saveConfig();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Removed &f" + args[2] + "&7 from &f" + args[0] + "&7's death-particles."));
				return true;
			}
			else {
				sender.sendMessage(ChatColor.GRAY + "This player doesn't have this particle.");
				return false;
			}
		}
		case "emote":
		{
			List<String> availableEmotes = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + targetUuid + ".EmotesAvailable");

			if (!Main.plugin.getAcceptedEmotes().contains(args[2])) {
				sender.sendMessage(ChatColor.GRAY + "Use one of these emotes:");
				String message = ChatColor.GRAY + "";
				for (String emote : Main.plugin.getAcceptedEmotes()) {
					message += emote + ", ";
				}
				sender.sendMessage(message);
				return false;
			}
			if (availableEmotes.contains(args[2])) {
				availableEmotes.remove(args[2]);
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + targetUuid + ".EmotesAvailable", availableEmotes);	
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").saveConfig();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Removed &f" + args[2] + "&7 from &f" + args[0] + "&7's emotes."));
				return true;
			}
			else {
				sender.sendMessage(ChatColor.GRAY + "This player doesn't have this emote.");
				return false;
			}
		}
		default: 
		{
			sender.sendMessage(ChatColor.RED + "/removeparticle [player] [arrow/death/emote] [particle]");
			return false;
		}
		}
	}
}

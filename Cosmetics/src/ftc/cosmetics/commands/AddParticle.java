package ftc.cosmetics.commands;

import ftc.cosmetics.Main;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidArgumentException;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import net.forthecrown.core.files.FtcUser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

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
	

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Valid use of command:
		if (args.length != 3) return false;

		FtcUser user;
		try {
			user = FtcCore.getUser(FtcCore.getOffOnUUID(args[0]));
		} catch (NullPointerException e){
			throw new InvalidPlayerInArgument(sender, args[0]);
		}
		
		
		switch (args[1]) {
		case "arrow":
		{
			List<Particle> arrowParticles = user.getParticleArrowAvailable();
			Particle bruh;
			try {
				bruh = Particle.valueOf(args[2]);
			} catch (NullPointerException e){ throw new InvalidArgumentException(sender, "Invalid particle"); }
			
			if (arrowParticles.contains(bruh)) {
				sender.sendMessage(ChatColor.GRAY + "This player already has this particle.");
				return true;
			}
			else if (!Main.plugin.getAcceptedArrowParticles().contains(bruh)) {
				sender.sendMessage(ChatColor.GRAY + "Use one of these particles:");
				String message = ChatColor.GRAY + "";
				for (Particle particle : Main.plugin.getAcceptedArrowParticles()) {
					message += particle.toString() + ", ";
				}
				throw new InvalidArgumentException(sender, message);
			}
			else {
				arrowParticles.add(bruh);
				user.setParticleArrowAvailable(arrowParticles);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Added &f" + args[2] + "&7 to &f" + user.getName() + "&7's arrow-particles."));
				return true;
			}
		}
		case "death":
		{
			List<String> availableEffects = user.getParticleDeathAvailable();
			
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
				throw new InvalidArgumentException(sender, message);
			}
			else {
				availableEffects.add(args[2]);
				user.setParticleDeathAvailable(availableEffects);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Added &f" + args[2] + "&7 to &f" + user.getName() + "&7's death-particles."));
				return true;
			}
		}
		case "emote":
		{
			if (user.getPlayer().hasPermission("ftc.emotes." + args[2])) {
				sender.sendMessage(ChatColor.GRAY + "This player already has this emote.");
				return false;
			}
			else if (!Main.plugin.getAcceptedEmotes().contains(args[2])) {
				sender.sendMessage(ChatColor.GRAY + "Use one of these emotes:");
				String message = ChatColor.GRAY + "";
				for (String emote : Main.plugin.getAcceptedEmotes()) {
					message += emote + ", ";
				}
				throw new InvalidArgumentException(sender, message);
			}
			else {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + user.getName() + " permission set ftc.emotes." + args[2]);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Added &f" + args[2] + "&7 to &f" + user.getName() + "&7's emotes."));
				return true;
			}
			
		}
		default: return false;
		}
	}
}

package ftc.chat.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Tpahere implements CommandExecutor{
	
	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Allows players to ask another player to teleport to them.
	 * Uses essentials.tpahere, this command just makes it pwetty.
	 * 
	 * Valid usages of command:
	 * [tpaskhere, tpahere]
	 * - /tpaskhere [player]
	 * 
	 * Permissions used:
	 * - ftc.tpahere
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * - (Essentials)
	 * 
	 * Main Author: Botul
	 * Edit by: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Checks if sender is a player.
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can do this.");
			return false;
		}
		Player player = (Player) sender;
		
		// Permission
		if (!player.hasPermission("ftc.tpahere")) {
			player.sendMessage(ChatColor.RED + "You don't have permission to do this!");
			return false;
		}
		
		// Valid command use
		if (args.length < 1 || args[0].equalsIgnoreCase(sender.getName())) {
			player.sendMessage(ChatColor.GRAY + "Usage: /tpahere [player]");
			return false;
		}
		
		Player playerThatGotRequest;
		try {
			playerThatGotRequest = Bukkit.getPlayer(args[0]);
		}
		catch (Exception e) {
			player.sendMessage(ChatColor.GRAY + args[0] + " is not online.");
			return false;
		}
		
		// Tpahere & sender message.
		TextComponent cancelTPA = new TextComponent(ChatColor.GRAY + "[X]");
		cancelTPA.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpacancel"));
		cancelTPA.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new Text("Cancel teleportation request.")));
		
		TextComponent sentRequest = new TextComponent(ChatColor.GOLD + "Request sent to " + ChatColor.YELLOW + playerThatGotRequest.getName() + ChatColor.GOLD + ". ");
		sentRequest.addExtra(cancelTPA);
		player.spigot().sendMessage(sentRequest);
		player.performCommand("essentials:tpahere " + playerThatGotRequest.getName());
		
		// Receiver message.
		TextComponent acceptTPA = new TextComponent(ChatColor.YELLOW + "[✔]");
		acceptTPA.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"));
		acceptTPA.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new Text("Accept teleportation request.")));
		
		TextComponent denyTPA = new TextComponent(ChatColor.GRAY + "[X]");
		denyTPA.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"));
		denyTPA.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new Text("Deny teleportation request.")));
						
		TextComponent yayOrNay = new TextComponent(ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " has requested that you teleport to them. ");
		yayOrNay.addExtra(acceptTPA);
		yayOrNay.addExtra(" ");
		yayOrNay.addExtra(denyTPA);
					
		playerThatGotRequest.spigot().sendMessage(yayOrNay);	
		
		return true;
	}
}

package me.wout.Senate;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {

	
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable() {
		
	}
	
	@EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) 
	{
		// Emoji stuff.
		if (event.getPlayer().hasPermission("ftc.donator3"))
		{
			String message = event.getMessage();
			message = message.replaceAll(":shrug:", "¯\\\\_(ツ)_/¯");
			message = message.replaceAll(":ughcry:", "(ಥ﹏ಥ)");
			message = message.replaceAll(":gimme:", "༼ つ ◕_◕ ༽つ");
			message = message.replaceAll(":gimmecry:", "༼ つ ಥ_ಥ ༽つ");
			message = message.replaceAll(":bear:", "ʕ• ᴥ •ʔ");
			message = message.replaceAll(":smooch:", "( ^ 3^) ♥");
			message = message.replaceAll(":why:", "ლ(ಠ益ಠლ)");
			message = message.replaceAll(":tableflip:", "(ノಠ益ಠ)ノ彡┻━┻");
			message = message.replaceAll(":tableput:", " ┬──┬ ノ( ゜-゜ノ)");
			message = message.replaceAll(":pretty:", "(◕‿◕ ✿)");
			message = message.replaceAll(":sparkle:", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧");
			message = message.replaceAll(":blush:", "(▰˘◡˘▰)");
			message = message.replaceAll(":sad:", "(._. )");
			event.setMessage(message);
			return;
		}
		
		// The sender is in the special world.
		if (event.getPlayer().getWorld().getName().contains(getConfig().getString("SenateWorld")))
		{
			event.setCancelled(true);
			event.getRecipients().clear();
			for (Player player : Bukkit.getWorld(getConfig().getString("SenateWorld")).getPlayers())
			{
				player.sendMessage(getPrefixFormat(event.getPlayer().getName()) + " " + ChatColor.GRAY + ChatColor.BOLD + ">" + ChatColor.RESET + " " + event.getMessage());
			}
			getServer().getConsoleSender().sendMessage("[SENATE] " + event.getPlayer().getName() + " > " + event.getMessage());
			return;
		}
		
		// The sender is not in the special world
		else 
		{
			// Remove all players in special world from recipients
			List<Player> recipientsToRemove = new ArrayList<>();
			for (Player recipient : event.getRecipients())
			{
				if (recipient.getWorld().getName().contains(getConfig().getString("SenateWorld")))
				{
					recipientsToRemove.add(recipient);
				}
			}
			for (Player player : recipientsToRemove)
			{
				event.getRecipients().remove(player);
			}
		}
		
	}
	
	private String getPrefixFormat(String playername)
	{
		switch (playername) {
		case "Wout":
			return ChatColor.YELLOW + "" + playername;
		default:
			return ChatColor.of("#FFFFA1") + "" + playername;
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("rlsenate")) {
			if (sender.isOp()) {
				this.reloadConfig();
				sender.sendMessage(ChatColor.GRAY + "Senate config reloaded.");
			} else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
			}
		}
		return true;
	}
}
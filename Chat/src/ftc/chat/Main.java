package ftc.chat;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.*;

import ftc.chat.commands.Bonk;
import ftc.chat.commands.Broadcast;
import ftc.chat.commands.Discord;
import ftc.chat.commands.Findpost;
import ftc.chat.commands.Help;
import ftc.chat.commands.Mwah;
import ftc.chat.commands.Poke;
import ftc.chat.commands.Posthelp;
import ftc.chat.commands.ReloadConfig;
import ftc.chat.commands.Scare;
import ftc.chat.commands.Spawn;
import ftc.chat.commands.ToggleEmotes;
import ftc.chat.commands.Tpa;
import ftc.chat.commands.Tpahere;
import ftc.chat.commands.EmoteTabCompleter;
import ftc.chat.commands.StaffChat;
import ftc.chat.commands.StaffChatToggle;

public class Main extends JavaPlugin implements Listener {
	
	public static Main plugin;
	
	public void onEnable() {
		plugin = this;
		
		// Config
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		// Events
		getServer().getPluginManager().registerEvents(this, this);
		
		// Commands
		getServer().getPluginCommand("bc").setExecutor(new Broadcast());
		getServer().getPluginCommand("rlchat").setExecutor(new ReloadConfig());
		getServer().getPluginCommand("aahelp").setExecutor(new Help()); //incomplete
		
		
		getServer().getPluginCommand("toggleemotes").setExecutor(new ToggleEmotes());
		getServer().getPluginCommand("bonk").setExecutor(new Bonk());
		getServer().getPluginCommand("mwah").setExecutor(new Mwah());
		getServer().getPluginCommand("poke").setExecutor(new Poke());
		getServer().getPluginCommand("scare").setExecutor(new Scare());
		
		
		getServer().getPluginCommand("discord").setExecutor(new Discord());
		getServer().getPluginCommand("findpost").setExecutor(new Findpost());
		getServer().getPluginCommand("posthelp").setExecutor(new Posthelp());
		getServer().getPluginCommand("spawn").setExecutor(new Spawn());
		
		getServer().getPluginCommand("tpask").setExecutor(new Tpa());
		getServer().getPluginCommand("tpaskhere").setExecutor(new Tpahere());
		
		getServer().getPluginCommand("sc").setExecutor(new StaffChat());
		getServer().getPluginCommand("sct").setExecutor(new StaffChatToggle());
		getServer().getPluginCommand("sc").setTabCompleter(new EmoteTabCompleter());
		
		
		// Timed announcements.
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			int counter = 0;
	        @Override
	        public void run() {
	        	String message = getPrefix() + ChatColor.translateAlternateColorCodes('&', (String) getConfig().getList("Announcements").get(counter));
	        	for (Player player : Bukkit.getOnlinePlayers()) 
	        	{
	        		// Don't broadcast info messages to players in the Senate.
	        		if (player.getWorld().getName().contains("senate")) continue;
	        		
	    			player.sendMessage(message);
	    			if (player.getWorld().getName().equalsIgnoreCase("world_resource"))
	    				player.sendMessage(ChatColor.GRAY + "You are in the resource world! To get back to the normal survival world, do /warp portal.");
	        	}
	        	if (getConfig().getList("Announcements").size() == counter+1) 
	        		counter = 0;
	        	else
	        		counter++;
	        }
	    }, 100, getDelay());
		
	}
	
	
	
	
	// Getters:
	
	public String getPrefix() {
		return ChatColor.translateAlternateColorCodes('&', getConfig().getString("Prefix"));
	}
	
	public String getDiscord() {
		return ChatColor.translateAlternateColorCodes('&', getConfig().getString("Discord"));
	}
	
	
	public Long getDelay() {
		return getConfig().getLong("DelayBetweenAnnouncements");
	}
	
	
	

	// Events:
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = (Player) event.getEntity();
		Location loc = player.getLocation();
		
		// Send where player died, but ignore world_void deaths.
		if (!loc.getWorld().getName().equalsIgnoreCase("world_void"))
			player.sendMessage(ChatColor.GRAY + "[FTC] You died at x=" + loc.getBlockX() + ", y=" + loc.getBlockY() + ", z=" + loc.getBlockZ() + ".");
		
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "! " + ChatColor.RESET + player.getName() + " died at x=" + loc.getBlockX() + ", y=" + loc.getBlockY() + ", z=" + loc.getBlockZ() + ", world=" + loc.getWorld().getName() + ".");
	}
	
	
	@EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) 
	{
		Player player = event.getPlayer();
		String playerName = player.getName();
		String message = event.getMessage();
		
		// Edit message to have emotes:
		if (player.hasPermission("ftc.donator3")) {
			message = message.replaceAll(":shrug:", "¯\\\\_(ツ)_/¯");
			message = message.replaceAll(":ughcry:", "(ಥ�?ಥ)");
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
			message = message.replaceAll(":pleased:", "(ᵔᴥᵔ)");
			event.setMessage(message);
		}
		
		// Handle players with staffchat toggled on: 
        if (Main.plugin.getConfig().getStringList("PlayersWithSCT").contains(player.getUniqueId().toString())) {
        	event.setCancelled(true);
    		
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (!(onlinePlayer.hasPermission("ftc.staffchat"))) continue;
                
                onlinePlayer.sendMessage(ChatColor.GRAY + "[Staff] " + playerName + ChatColor.GRAY + ChatColor.BOLD + " > " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', message));
                System.out.println(playerName + " issued server command: /sc " + ChatColor.translateAlternateColorCodes('&', message));
            }
            return;
        }
        
	
		// The sender is in the Senate world.
		if (player.getWorld().getName().contains("senate")) {
			event.setCancelled(true);
			event.getRecipients().clear();
			String prettyPlayerName;
			
			// Give everyone a yellow name in chat.
			switch (playerName) 
			{
				case "Wout":
					prettyPlayerName = ChatColor.YELLOW + "" + playerName;
					break;
				default:
					prettyPlayerName = net.md_5.bungee.api.ChatColor.of("#FFFFA1") + "" + playerName;
			}
			
			for (Player senator : Bukkit.getWorld("world_senate").getPlayers()) {
				senator.sendMessage(prettyPlayerName + " " + ChatColor.GRAY + ChatColor.BOLD + ">" + ChatColor.RESET + " " + message);
			}
			this.getServer().getConsoleSender().sendMessage("[SENATE] " + playerName + " > " + message);
			return;
		}
		
		// The sender is not in the Senate world, remove all players in Senate world from recipients:
		else {
			List<Player> recipientsToRemove = new ArrayList<>();
			
			for (Player recipient : event.getRecipients())
			{
				if (recipient.getWorld().getName().contains("senate")) recipientsToRemove.add(recipient);
			}
			for (Player playerToRemove : recipientsToRemove) event.getRecipients().remove(playerToRemove);
			return;
		}
	}
	
	
}

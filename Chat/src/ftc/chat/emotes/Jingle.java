package ftc.chat.emotes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ftc.chat.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Jingle implements CommandExecutor {
	
	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Command that allows players to vibe on Jingle Bells.
	 * Only works if they both have emotes enabled.
	 * 
	 * Valid usages of command:
	 * - /jingle
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * 
	 * Main Author: Wout
	 */

    List<String> onCooldown = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	
    	// Sender must be player:
        if (!(sender instanceof Player)) 
        {
        	sender.sendMessage("Only players can use this command"); 
        	return false;
        }
        
        Player player = (Player) sender;
        
        // Sender can't be on cooldown:
        if (onCooldown.contains(player.getName())) 
        {
        	player.sendMessage(ChatColor.GRAY + "You jingle too often lol");
        	player.sendMessage(ChatColor.DARK_GRAY + "This only works every 6 seconds.");
        	return false;
        }
        
        
        /*if (!Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + player.getUniqueId().toString() + ".EmotesAvailable").contains("JINGLE")) {
        	player.sendMessage(ChatColor.GRAY + "You haven't unlocked this emote yet.");
        	return false;
        }*/
        
        // Command no args:
        if (args.length < 1 || args[0].equalsIgnoreCase(sender.getName()))
        {
        	cooldown(player.getName());
            jingle(player);
        	return true;
        }

        
        // Both sender and target should have emotes enabled:
        if (Main.plugin.getConfig().getStringList("NoEmotes").contains(player.getUniqueId().toString())) {
        	player.sendMessage(ChatColor.GRAY + "You've emotes turned off.");
        	player.sendMessage(ChatColor.GRAY + "Do " + ChatColor.RESET + "/toggleemotes" + ChatColor.GRAY + " to enable them.");
        	
        	return false;
        }
        
        Player target = Bukkit.getServer().getPlayer(args[0]);
        if (target == null) 
        {
        	player.sendMessage(args[0] + " isn't a currently online player");
        	return false;
        }
        if (Main.plugin.getConfig().getStringList("NoEmotes").contains(target.getUniqueId().toString())) {
        	player.sendMessage(ChatColor.GRAY + "This player has disabled emotes.");
        	player.sendMessage(ChatColor.GRAY + "They can do " + ChatColor.RESET + "/toggleemotes" + ChatColor.GRAY + " to enable them.");
        	return false;
        }

        // Actual jingling:
        player.sendMessage("You've sent " + ChatColor.YELLOW + target.getName() + ChatColor.RESET + " a sick Christmas beat!");
        target.sendMessage("You've received jingle vibes from " + ChatColor.YELLOW + player.getName() + ChatColor.RESET + "!");

        cooldown(player.getName());
        jingle(target);

        return true;
    }
    
    private void cooldown(String name) {
    	onCooldown.add(name);
        new BukkitRunnable() {
            @Override
            public void run() {
                onCooldown.remove(name);
            }
        }.runTaskLater(Main.plugin, 20 * 6);
    }
    
    private void jingle(Player player) {
    	Location loc = player.getLocation();
    	loc.getWorld().spawnParticle(Particle.SNOW_SHOVEL,loc, 25, 0.1, 0, 0.1, 1);
    	loc.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 0.1, 0, 0.1, 0.1);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			@Override
			public void run() {
				//b = bass //s = snare
		    	 playSound(0, loc, midTone); //b
		    	 playSound(4, loc, midTone); //s
		    	 playSound(8, loc, midTone); //b
		    	 
		    	 playSound(16, loc, midTone); //b
		    	 playSound(20, loc, midTone); //s
		    	 playSound(24, loc, midTone); //b
		    	 
		    	 playSound(32, loc, midTone); //b
		    	 playSound(36, loc, 1.8f); //s
		    	 playSound(40, loc, 1.2f); //b
		    	 playSound(44, loc, midTone); //s
		    	 
		    	 playSound(48, loc, midTone); //b
		    	 playSound(52, loc, midTone); //s
		    	 playSound(56, loc, midTone); //b
		    	 
		    	 
		    	 playSound(64, loc, highTone); //b
		    	 playSound(68, loc, highTone); //s
		    	 playSound(72, loc, highTone); //b
		    	 
		    	 playSound(78, loc, highTone); //s
		    	 playSound(80, loc, highTone); //b
		    	 playSound(84, loc, midTone); //s
		    	 playSound(88, loc, midTone); //b
		    	 
		    	 playSound(96, loc, midTone);  //b
		    	 playSound(100, loc, 1.3f); //s
		    	 playSound(104, loc, 1.3f); //s
		    	 playSound(108, loc, 1.7f); //s 
		    	 playSound(112, loc, midTone); //s
		    	 playSound(120, loc, 2.0f);
		    }
		}, 8);
    }
    
    private Set<Integer> bass = new HashSet<>(Arrays.asList(
    		0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 80, 88, 96));
    private Set<Integer> snare = new HashSet<>(Arrays.asList(
    		4, 20, 36, 44, 52, 68, 78, 84, 100, 104, 108, 112));
    private float midTone = 1.5f;
    private float highTone = 1.7f;
    
    private void playSound(int delay, Location loc, float pitch) {
    	Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			@Override
			public void run() {
				if (bass.contains(delay)) {
					loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.MASTER, 0.2F, 1F);
				}
				else if (snare.contains(delay)) {
					loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_SNARE, SoundCategory.MASTER, 1F, 1F);
				}
		        loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.MASTER, 1F, pitch);
		    }
		}, delay);
    }
}

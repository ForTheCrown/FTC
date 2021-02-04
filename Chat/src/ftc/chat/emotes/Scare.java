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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import ftc.chat.Main;

import java.util.ArrayList;
import java.util.List;

public class Scare implements CommandExecutor {
	
	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Command that allows players to scare another player.
	 * Only works if they both have emotes enabled.
	 * 
	 * Valid usages of command:
	 * - /scare
	 * - /scare [target]
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
        
        // Sender can't be on cooldown:
        if (onCooldown.contains(sender.getName())) 
        {
        	sender.sendMessage(ChatColor.GRAY + "You scare people too often lol");
        	sender.sendMessage(ChatColor.DARK_GRAY + "This only works every 30 seconds.");
        	return false;
        }
        
        Player player = (Player) sender;
        
        if (!Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + player.getUniqueId().toString() + ".EmotesAvailable").contains("SCARE")) {
        	player.sendMessage(ChatColor.GRAY + "You haven't unlocked this emote yet.");
        	return false;
        }
        
        // Command no args:
        if (args.length < 1 || args[0].equalsIgnoreCase(sender.getName()))
        {
            scare(player);
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

        // Actual scaring:
        player.sendMessage("You scared " + ChatColor.YELLOW + target.getName() + ChatColor.RESET + "!");
        target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.RESET + " scared you!");

        scare(target);

        // Put sender on cooldown:
        onCooldown.add(player.getName());
        new BukkitRunnable() {
            @Override
            public void run() {
                onCooldown.remove(player.getName());
            }
        }.runTaskLater(Main.plugin, 20 * 20);

        return true;
    }
    
    private void scare(Player player) {
    	Location loc = player.getLocation();
    	player.spawnParticle(Particle.MOB_APPEARANCE, loc.getX(), loc.getY(), loc.getZ(), 1);
    	Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			@Override
			public void run() {
				 player.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.MASTER, 2.0F, 1F);
		    	 
		    	 player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 9, false, false, false));
		    	 
		    	 for (int i = 0; i < 3; i++) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
						@Override
						public void run() {
					        player.playSound(loc, Sound.ENTITY_ENDERMAN_SCREAM, SoundCategory.MASTER, 1.5F, 1F);
					    }
					}, i* 3L);
		    	 }
		    }
		}, 3L);
    }
}

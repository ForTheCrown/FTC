package ftc.chat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ftc.chat.Main;

import java.util.ArrayList;
import java.util.List;

public class Bonk implements CommandExecutor {
	
	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Command that allows players to bonk another player.
	 * Only works if they both have emotes enabled.
	 * 
	 * Valid usages of command:
	 * - /bonk
	 * - /bonk [target]
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * 
	 * Main Author: Botul
	 * Edit by: Wout
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
        	sender.sendMessage(ChatColor.GRAY + "You bonk people too often lol");
        	return false;
        }
        
        // Command no args:
        if (args.length < 1 || args[0].equalsIgnoreCase(sender.getName()))
        {
        	sender.sendMessage("Don't hurt yourself ♥");
        	return true;
        }

        
        // Both sender and target should have emotes enabled:
        Player player = (Player) sender;
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

        // Actual bonking:
        Location loc = target.getLocation();
        loc.setPitch(loc.getPitch() + 20F);

        player.sendMessage("You bonked " + ChatColor.YELLOW + target.getName() + ChatColor.RESET + "!");
        target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.RESET + " bonked you!");

        target.teleport(loc);
        target.getWorld().playSound(loc, Sound.ENTITY_SHULKER_HURT_CLOSED, 2.0F, 0.8F);
        target.getWorld().spawnParticle(Particle.CRIT, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);

        // Put sender on cooldown:
        if(!player.isOp()){
            onCooldown.add(player.getName());
            new BukkitRunnable() {
                @Override
                public void run() {
                    onCooldown.remove(player.getName());
                }
            }.runTaskLater(Main.plugin, 20 * 5);
        }

        return true;
    }
}

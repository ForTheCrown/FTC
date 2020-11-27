package ftc.chat.emotes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ftc.chat.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Poke implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Command that allows players to poke another player.
	 * Only works if they both have emotes enabled.
	 * 
	 * 
	 * Valid usages of command:
	 * - /poke
	 * - /poke [player]
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * 
	 * Main Author: Botul
	 * Edit by: Wout
	 */
	
    List<String> onCooldown = new ArrayList<>();
    List<String> pokeOwies = Arrays.asList("stomach", "back", "arm", "butt", "cheek", "neck");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	// Sender must be a player:
        if (!(sender instanceof Player)) 
        {  
        	sender.sendMessage("Only players may execute this command."); 
        	return false;
        }
        
        // Sender can't be on cooldown:
        if (onCooldown.contains(sender.getName())) 
        {
        	sender.sendMessage(ChatColor.GRAY + "You poke people too often."); 
        	return false;
        }

        Player player = (Player) sender;
        
        // Sender should have emotes enabled:
        if (Main.plugin.getConfig().getStringList("NoEmotes").contains(player.getUniqueId().toString())) {
        	player.sendMessage(ChatColor.GRAY + "You've emotes turned off.");
        	player.sendMessage(ChatColor.GRAY + "Do " + ChatColor.RESET + "/toggleemotes" + ChatColor.GRAY + " to enable them.");
        	
        	return false;
        }
        
        // Command no args or target = sender:
        if (args.length < 1 || args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage("You poked yourself! Weirdo"); //Damn, some people really be weird, pokin themselves, couldn't be me ( ._.)
            player.getWorld().playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 3.0F, 1.8F);
            return true;
        }
        
        
        // Target should have emotes enabled:
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null) {
        	player.sendMessage(args[0] + ChatColor.GRAY + " isn't online at the moment."); 
        	return false;
        }
        if (Main.plugin.getConfig().getStringList("NoEmotes").contains(target.getUniqueId().toString())) {
        	player.sendMessage(ChatColor.GRAY + "This player has disabled emotes.");
        	player.sendMessage(ChatColor.GRAY + "They can do " + ChatColor.RESET + "/toggleemotes" + ChatColor.GRAY + " to enable them.");
        	return false;
        }

        // Actual poking:
        int pokeOwieInt = (int)(Math.random()*pokeOwies.size()); //The random int that determines what body part they'll poke lol
        player.sendMessage("You poked " + ChatColor.YELLOW + target.getName() + "'s " + ChatColor.RESET + pokeOwies.get(pokeOwieInt));

        target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.RESET + " poked your " + pokeOwies.get(pokeOwieInt));
        target.getWorld().playSound(target.getLocation(), Sound.ENCHANT_THORNS_HIT, 3.0F, 1.8F);
        target.setVelocity(target.getVelocity().add(target.getLocation().getDirection().normalize().multiply(-0.3).setY(.1)));

        
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

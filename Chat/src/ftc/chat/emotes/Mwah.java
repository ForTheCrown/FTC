package ftc.chat.emotes;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ftc.chat.Main;

import java.util.ArrayList;
import java.util.List;

public class Mwah implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Command that allows players to send a kiss to another player.
	 * Only works if they both have emotes enabled.
	 * 
	 * 
	 * Valid usages of command:
	 * - [kiss, mwah, smooch]
	 * - /mwah
	 * - /mwah [player]
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
        // Sender must be a player:
    	if (!(sender instanceof Player)) 
        {
        	sender.sendMessage("Only players may execute this command."); 
        	return false;
        }
        
    	// Sender can't be on cooldown:
        if (onCooldown.contains(sender.getName())) 
        {
        	sender.sendMessage(ChatColor.GRAY + "You kiss too often :D"); 
        	return false;
        }

		Player player = (Player) sender;
        Location loc = player.getLocation();

        // Sender should have emotes enabled:
		if (Main.plugin.getConfig().getStringList("NoEmotes").contains(player.getUniqueId().toString())) 
		{
        	player.sendMessage(ChatColor.GRAY + "You've emotes turned off.");
        	player.sendMessage(ChatColor.GRAY + "Do " + ChatColor.RESET + "/toggleemotes" + ChatColor.GRAY + " to enable them.");
        	
        	return false;
        }
		
		 // Command no args or target = sender:
        if (args.length < 1 || args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.YELLOW + "Love yourself!" + ChatColor.RESET + " ( ^ 3^) ♥");
            player.playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
            player.spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
            return true;
        }
		
        // Target should have emotes enabled:
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) 
        {
        	player.sendMessage(args[0] + ChatColor.GRAY + " isn't online at the moment."); 
        	return false;
        }
        if (Main.plugin.getConfig().getStringList("NoEmotes").contains(target.getUniqueId().toString())) {
        	player.sendMessage(ChatColor.GRAY + "This player has disabled emotes.");
        	player.sendMessage(ChatColor.GRAY + "They can do " + ChatColor.RESET + "/toggleemotes" + ChatColor.GRAY + " to enable them.");
        	return false;
        }

        // Actual smooching:
        player.sendMessage(ChatColor.RED + "♥" + ChatColor.RESET + " You smooched " + target.getName() + ChatColor.RED + " ♥");

        TextComponent mwahBack = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&c♥ &e" + player.getName() + " &rsmooched you! &r( ^ 3^) &c♥"));
        mwahBack.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mwah " + player.getName()));
        mwahBack.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to smooch them back")));
        target.spigot().sendMessage(mwahBack);

        Location targetLoc = target.getLocation();
        target.getWorld().playSound(targetLoc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
        target.getWorld().spawnParticle(Particle.HEART, targetLoc.getX(), targetLoc.getY()+1, targetLoc.getZ(), 5, 0.5, 0.5, 0.5);

        loc.getWorld().spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
        loc.getWorld().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);

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
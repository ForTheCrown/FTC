package me.wout.Pirate.commands;

import me.wout.Pirate.Main;
import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ghtargetshowname implements CrownCommandExecutor {
	
	public ghtargetshowname() {
		Main.plugin.getCommandHandler().registerCommand("GHTargetShowName", this);
	}
	
	@Override
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
		
		Player player = (Player) sender;
		
		for (Entity nearbyEntity : player.getWorld().getNearbyEntities(player.getLocation(), 2, 2, 2)) {
			if (nearbyEntity.getType() != EntityType.ARMOR_STAND) continue;
			
			if (nearbyEntity.getCustomName() != null && nearbyEntity.getCustomName().contains("GHTargetStand")) {
				if (nearbyEntity.isCustomNameVisible()) nearbyEntity.setCustomNameVisible(false);
				else nearbyEntity.setCustomNameVisible(true);
				return true;				
			}
		}
		return false;
	}
}

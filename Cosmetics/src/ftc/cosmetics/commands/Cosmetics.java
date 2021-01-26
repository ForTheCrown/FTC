package ftc.cosmetics.commands;

import ftc.cosmetics.Main;
import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.core.files.FtcUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cosmetics implements CrownCommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Opens the Cosmetics menu
	 * 
	 * 
	 * Valid usages of command:
	 * - /cosmetics
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * 
	 * Author: Wout
	 */
	
	@Override
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
		
		Player player = (Player) sender;
		FtcUser user = FtcCore.getUser(player.getUniqueId());
		player.openInventory(Main.plugin.getMainCosmeticInventory(user));
		
		return true;
	}
}

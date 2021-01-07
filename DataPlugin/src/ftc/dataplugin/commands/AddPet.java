package ftc.dataplugin.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ftc.dataplugin.DataPlugin;
import ftc.dataplugin.FtcUserData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AddPet implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * This command is used to add a pet to a player
	 *
	 *
	 * Valid usages of command:
	 * - /addpet <player> <pet>
	 *
	 * Permissions used:
	 * - ftc.addpet
	 *
	 * Referenced other classes:
	 * - DataPlugin: DataPlugin.plugin
	 *
	 * Author: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Valid use of command
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED + "/addpet <player> <pet>");
			return false;
		}
		
		// Valid pet
		String[] temp = {"gray_parrot", "green_parrot", "blue_parrot"};
		List<String> acceptedPets = new ArrayList<>(Arrays.asList(temp));
		if (!acceptedPets.contains(args[1])) 
		{
			sender.sendMessage(ChatColor.RED + "Invalid pet, use of one these:");
			String message = ChatColor.GRAY + "";
			for (String pet : temp)
				message += pet + ", ";
			sender.sendMessage(message);
			return false;
		}
		
		// Valid player
		UUID playeruuid = DataPlugin.trySettingUUID(args[0]);
		if (playeruuid == null)
		{
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid playername.");
			return false;
		}

		FtcUserData targetData = DataPlugin.getUserData(Bukkit.getPlayer(args[0]));
		
		// Add pet
		List<String> pets = targetData.getPets();
		if (pets.contains(args[1]))
		{
			sender.sendMessage(ChatColor.GRAY + args[0].toLowerCase() + " already has the " + args[1] + " pet.");
			return false;
		}
		pets.add(args[1]);
		targetData.setPets(pets);
		targetData.saveUserData();

		sender.sendMessage(ChatColor.GRAY + "Added " + args[1] + " to their pet list.");
		return true;
	}

}

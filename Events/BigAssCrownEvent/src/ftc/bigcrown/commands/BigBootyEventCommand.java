package ftc.bigcrown.commands;

import ftc.bigcrown.Main;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BigBootyEventCommand implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Base command with all the args for the BigAss Event we're doing lol
     * You can create locations for the gifts to spawn at,
     * trigger their spawning and
     * reload the config
     *
     * Valid usages of command:
     * - /bbe setloc (sets the possible location of a present to where the player is standing)
     * - /bbe reload
     * - /bbe setchallenge <challengeName>
     * - /bbe stoploop
     * - /bbe startloop
     *
     *
     * Permissions used:
     * - bbe.admin
     *  Using it just cuz an if player.isOp is too much work to write lol
     *
     * Referenced other classes:
     * - Main: Main.plugin
     *
     * Author: That Crossdressing Estonian xD (Ants)
     */

	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may execute this command");
            return false;
        }
        Player player = (Player) sender;
        if(args.length < 1) {player.sendMessage("Mssing arguments"); return false; }

        switch (args[0]) {
        	// Adds location to present list.
            case "setloc":
                List<Location> locationList;
                if(Main.plugin.getLocationList() != null) {
                    locationList = Main.plugin.getLocationList();
                } else{
                    locationList = new ArrayList<>();
                }
                Location playerLoc = player.getLocation();
                Location locToAdd = new Location(playerLoc.getWorld(), playerLoc.getBlockX(), playerLoc.getBlockY(), playerLoc.getBlockZ());
                for (Location previouslyDefinedLoc : locationList) {
                	if (previouslyDefinedLoc.getBlockX() == locToAdd.getBlockX()
                			&& previouslyDefinedLoc.getBlockY() == locToAdd.getBlockY()
                			&& previouslyDefinedLoc.getBlockZ() == locToAdd.getBlockZ()) {
                		player.sendMessage(ChatColor.GRAY + "This location is already in the list.");
                		return false;
                	}
                }
                
                locationList.add(locToAdd);
                Main.plugin.getConfig().set("PresentList", locationList);
                player.sendMessage(ChatColor.GRAY + "Your location has been added to the PresentList.");
                Main.plugin.saveConfig();
                return true;

            // Saves a name and the current location as a challenge.
            case "setchallenge":
            	    if(args.length < 2) {
                    player.sendMessage(ChatColor.GRAY + "/bbe setchallenge <challengeName>");
                    return false;
                }
            	// Add name to challenge list
                String challangeName = args[1].toUpperCase();
                List<String> challenges = Main.plugin.getConfig().getStringList("ChallengeList");
                challenges.add(challangeName);
                Main.plugin.getConfig().set("ChallengeList", challenges);
                
                // Add location to new challenge
                Main.plugin.getConfig().getConfigurationSection("ChallengeList." + challangeName).set("Location", player.getLocation());
                
                Main.plugin.saveConfig();
                return true;

            //
            case "usechallenge":
                if(args.length < 2 || args[1] == null){
                    player.sendMessage(ChatColor.GRAY + "/bbe usechallenge <challengeName>");
                    return false;
                }

                if(!Main.plugin.getConfig().getStringList("ChallengeList").contains(args[1])){
                    player.sendMessage(ChatColor.GRAY + "That entry doesn't exist");
                    return false;
                }
                Location loc = (Location) Main.plugin.getConfig().getConfigurationSection("ChallengeList." + args[1] + ".Location");
                player.teleport(loc);
                break;
            
            case "startloop":
            	Main.plugin.runLoop = true;
                Main.plugin.loop();
                player.sendMessage(ChatColor.GRAY + "Present spawning loop has been started");
                return true;

            case "stoploop":
                Main.plugin.stopLoop();
                player.sendMessage(ChatColor.GRAY + "Present spawning loop has been stopped");
                return true;

            case "reload":
                Main.plugin.reloadConfig();
                player.sendMessage(ChatColor.GRAY + "Plugin's config has been reloaded");
                break;
             default:
            	 break;
        }
        return false;
    }
}

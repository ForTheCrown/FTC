package ftc.bigcrown.commands;

import ftc.bigcrown.Main;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * - /bbe setloc (sets the poossible location of a present to where the player is standing)
     * - /bbe reload
     * - /bbe setchallange
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
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players may execute this command");
            return false;
        }
        Player player = (Player) sender;
        if(args.length < 1){player.sendMessage("Mssing arguments"); return false; }

        switch (args[0]){
            case "setloc":
                List<Location> locationList;
                if(Main.plugin.getConfig().getList("PresentList") != null){
                    locationList = (List<Location>) Main.plugin.getConfig().getList("PresentList");
                } else{
                    locationList = new ArrayList<>();
                }
                locationList.add(player.getLocation());
                Main.plugin.getConfig().set("PresentList", locationList);
                player.sendMessage("Your location has been added to the PresentList");
                Main.plugin.saveConfig();
                return true;

            case "setchallange":
                if(args.length < 2){
                    player.sendMessage("Too little arguments");
                    return false;
                }
                String challangeName = args[1].toUpperCase();

                Main.plugin.getConfig().createSection("ChallengeList." + challangeName);
                Main.plugin.getConfig().getConfigurationSection("ChallengeList." + challangeName).set("Location", player.getLocation());
                Main.plugin.saveConfig();
                return true;

            case "usechallange":
                if(args.length < 2){
                    player.sendMessage("You must specify a challenge ID to use");
                    return false;
                }
                int chalID = Integer.parseInt(args[1]);

                if(chalID >= Main.plugin.getConfig().getList("ChallangeList").size()){
                    player.sendMessage("That entry doesn't exist");
                    return false;
                }
                Location loc = (Location) Main.plugin.getConfig().getList("ChallengeList").get(chalID);
                player.teleport(loc);
                break;
            case "startloop":
                Main.plugin.startLoop();
                player.sendMessage("Present spawning loop has been started");
                return true;

            case "stoploop":
                Main.plugin.stopLoop();
                player.sendMessage("Present spawning loop has been stopped");
                return true;

            case "reload":
                Main.plugin.reloadConfig();
                player.sendMessage("Plugin's config has been reloaded");
                break;
        }
        return false;
    }
}
